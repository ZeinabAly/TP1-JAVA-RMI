package client.src.client.logic;

import common.src.common.*;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Logique client : les opérations sur les interfaces distantes, indépendantes de l'interface utilisateur.
 * La CLI et la GUI s'appuient toutes deux sur cette classe. Elle ne laisse sortir ni type RMI ni RemoteException :
 * toute panne distante devient une ClientException au message compréhensible.
 */
public class CabinetClient implements AutoCloseable {

    @FunctionalInterface
    private interface Appel<T> {
        T executer() throws RemoteException;
    }

    @FunctionalInterface
    private interface Action {
        void executer() throws RemoteException;
    }

    private final CabinetVet cabinet;
    private volatile AlerteObserverImpl observateur; // null tant qu'on n'est pas abonné

    /** Le stub est injecté : le lookup n'a pas lieu ici. */
    public CabinetClient(CabinetVet cabinet) {
        this.cabinet = cabinet;
    }

    // ------------------------------------------------------------------ patients

    public List<PatientVue> listerPatients() throws ClientException {
        return appel(() -> {
            List<PatientVue> vues = new ArrayList<>();
            for (Animal a : cabinet.getPatients()) {
                vues.add(vue(a));
            }
            return vues;
        });
    }

    public Optional<PatientVue> rechercher(String nom) throws ClientException {
        return appel(() -> {
            Animal a = cabinet.rechercherParNom(nom);
            return a == null ? Optional.<PatientVue>empty() : Optional.of(vue(a));
        });
    }

    public PatientVue enregistrer(String nom, String maitre, String race, String espece, int esperanceVie)
            throws ClientException {
        // Seules des données traversent le réseau : l'objet distant est créé côté serveur (A5).
        return appel(() -> vue(cabinet.ajouterPatient(nom, maitre, race, new Espece(espece, esperanceVie))));
    }

    public boolean supprimer(String nom) throws ClientException {
        return appel(() -> cabinet.supprimerPatient(nom));
    }

    public int nombrePatients() throws ClientException {
        return appel(cabinet::getNombrePatients);
    }

    // ------------------------------------------------------------------ dossier de suivi

    public DossierVue consulterDossier(String nomPatient) throws ClientException {
        Animal animal = trouver(nomPatient);
        return appel(() -> {
            DossierSuivi dossier = animal.getDossier(); // stub : on agit sur le dossier du serveur
            List<ObservationVue> historique = new ArrayList<>();
            for (Consultation c : dossier.getHistorique()) {
                historique.add(new ObservationVue(c.getDate(), c.getTexte()));
            }
            return new DossierVue(nomPatient, dossier.getEtatSante(), historique);
        });
    }

    public void majEtatSante(String nomPatient, String etat) throws ClientException {
        Animal animal = trouver(nomPatient);
        action(() -> animal.getDossier().setEtatSante(etat));
    }

    public void ajouterObservation(String nomPatient, String texte) throws ClientException {
        Animal animal = trouver(nomPatient);
        action(() -> animal.getDossier().ajouterObservation(new Consultation(LocalDateTime.now(), texte)));
    }

    // ------------------------------------------------------------------ alertes

    /** S'abonne aux alertes. L'écouteur est appelé sur un fil RMI : à l'interface de se replacer sur son propre fil. */
    public synchronized void sAbonner(Consumer<String> ecouteur) throws ClientException {
        if (observateur != null) {
            throw new ClientException("Vous êtes déjà abonné aux alertes.");
        }
        AlerteObserverImpl o = appel(() -> new AlerteObserverImpl(ecouteur)); // exporte l'objet
        try {
            action(() -> cabinet.abonner(o));
            observateur = o;
        } catch (ClientException e) {
            desexporter(o);
            throw e;
        }
    }

    public synchronized void seDesabonner() throws ClientException {
        AlerteObserverImpl o = observateur;
        if (o == null) {
            throw new ClientException("Vous n'êtes pas abonné aux alertes.");
        }
        observateur = null;
        try {
            action(() -> cabinet.desabonner(o));
        } finally {
            desexporter(o);
        }
    }

    public boolean estAbonne() {
        return observateur != null;
    }

    /**
     * Fermeture propre : désabonnement puis désexport de l'observateur.
     * Un objet exporté maintient la JVM en vie : sans cela, le processus ne se terminerait jamais.
     */
    @Override
    public synchronized void close() {
        AlerteObserverImpl o = observateur;
        if (o == null) return;
        observateur = null;
        try {
            cabinet.desabonner(o);
        } catch (RemoteException e) {
            // serveur absent : sans importance à la fermeture
        }
        desexporter(o);
    }

    // ------------------------------------------------------------------ interne

    private Animal trouver(String nom) throws ClientException {
        Animal a = appel(() -> cabinet.rechercherParNom(nom));
        if (a == null) {
            throw new ClientException("Aucun patient nommé '" + nom + "'.");
        }
        return a;
    }

    private static PatientVue vue(Animal a) throws RemoteException {
        Espece e = a.getEspece(); // copie
        return new PatientVue(a.getNom(), a.getNomMaitre(), a.getRace(), e.getNom(), e.getEsperanceVie());
    }

    private static void desexporter(Remote o) {
        try {
            UnicastRemoteObject.unexportObject(o, true);
        } catch (NoSuchObjectException e) {
            // déjà désexporté
        }
    }

    private void action(Action a) throws ClientException {
        appel(() -> {
            a.executer();
            return null;
        });
    }

    /** Traduit toute défaillance distante en message compréhensible. */
    private <T> T appel(Appel<T> a) throws ClientException {
        try {
            return a.executer();
        } catch (ConnectException | ConnectIOException | NoSuchObjectException e) {
            throw new ClientException("Serveur injoignable. Vérifiez qu'il est démarré, puis réessayez.", e);
        } catch (ServerException e) {
            throw new ClientException("Le serveur a échoué à traiter la demande : " + causeRacine(e), e);
        } catch (RemoteException e) {
            throw new ClientException("Communication interrompue avec le serveur (" + e.getClass().getSimpleName()
                    + "). Il est peut-être arrêté.", e);
        } catch (IllegalArgumentException e) { // refus métier du serveur
            throw new ClientException(e.getMessage(), e);
        }
    }

    private static String causeRacine(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getClass().getSimpleName() + (t.getMessage() != null ? " : " + t.getMessage() : "");
    }
}
