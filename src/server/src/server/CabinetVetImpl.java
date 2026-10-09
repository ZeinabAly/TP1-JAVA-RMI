package server.src.server;

import common.src.common.*;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Servant du cabinet : collection de patients, abonnés aux alertes et seuils. */
public class CabinetVetImpl extends UnicastRemoteObject implements CabinetVet {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int[] SEUILS = {100, 500, 1000};

    private final transient List<AnimalImpl> animaux = new ArrayList<>(); // protégée par "this"

    // CopyOnWriteArrayList : on peut parcourir les abonnés pendant qu'un autre client s'abonne ou se désabonne.
    private final transient List<AlerteObserver> observateurs = new CopyOnWriteArrayList<>();

    // Les rappels sont envoyés par un fil dédié : un client lent ne bloque pas l'appelant.
    private final transient ExecutorService diffusion = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "diffusion-alertes");
        t.setDaemon(true);
        return t;
    });

    public CabinetVetImpl() throws RemoteException {
        super();
    }

    /** Usage serveur uniquement (peuplement initial) : ne déclenche aucune alerte. */
    public synchronized void ajouterInitial(AnimalImpl a) {
        animaux.add(a);
    }

    @Override
    public synchronized List<Animal> getPatients() throws RemoteException{
        return new ArrayList<Animal>(animaux); // copie : les éléments sont remplacés par des stubs à l'envoi
    }

    @Override
    public synchronized Animal rechercherParNom(String nom) throws RemoteException {
        return trouver(nom);
    }

    private AnimalImpl trouver(String nom) throws RemoteException {
        for (AnimalImpl a : animaux) {
            if (a.getNom().equalsIgnoreCase(nom)) return a;
        }
        return null;
    }

    @Override
    public Animal ajouterPatient(String nom, String nomMaitre, String race, Espece espece) throws RemoteException {
        if (vide(nom) || vide(nomMaitre) || vide(race) || espece == null || vide(espece.getNom())) {
            throw new IllegalArgumentException("Nom, maître, race et espèce sont obligatoires.");
        }
        AnimalImpl a;
        int avant;
        int apres;
        synchronized (this) {
            if (trouver(nom) != null) {
                throw new IllegalArgumentException("Un patient nommé '" + nom + "' existe déjà.");
            }
            avant = animaux.size();
            a = new AnimalImpl(nom, nomMaitre, race, espece, new DosSuiviImpl("Non évalué"));
            animaux.add(a);
            apres = animaux.size();
        }
        verifierSeuils(avant, apres); // hors verrou : on ne tient pas le verrou pendant des appels réseau
        return a;
    }

    @Override
    public boolean supprimerPatient(String nom) throws RemoteException {
        int avant;
        int apres;
        synchronized (this) {
            avant = animaux.size();
            animaux.removeIf(a -> {
                try {
                    return a.getNom().equals(nom);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
            apres = animaux.size();
        }
        if (apres == avant) return false;
        verifierSeuils(avant, apres);
        return true;
    }

    @Override
    public synchronized int getNombrePatients() throws RemoteException { return animaux.size(); }

    @Override
    public void abonner(AlerteObserver o) throws RemoteException {
        if (!observateurs.contains(o)) observateurs.add(o);
    }

    @Override
    public void desabonner(AlerteObserver o) throws RemoteException {
        observateurs.remove(o);
    }

    @Override
    public int getNombreObservateurs() throws RemoteException { return observateurs.size(); }

    private void verifierSeuils(int avant, int apres) {
        for (int seuil : SEUILS) {
            boolean monte = avant < seuil && apres >= seuil;
            boolean descend = avant >= seuil && apres < seuil;
            if (monte || descend) {
                diffuser("Seuil de " + seuil + " patients franchi à la " + (monte ? "hausse" : "baisse")
                        + " (" + apres + " patients).");
            }
        }
    }

    private void diffuser(String message) {
        diffusion.execute(() -> {
            for (AlerteObserver o : observateurs) {
                try {
                    o.notifier(message);
                } catch (RemoteException e) {
                    // Client fermé ou injoignable : on le retire, les autres continuent d'être prévenus.
                    observateurs.remove(o);
                    System.out.println("Observateur injoignable retiré (" + e.getClass().getSimpleName() + ").");
                } catch (RuntimeException e) {
                    System.out.println("Erreur inattendue chez un observateur : " + e);
                }
            }
        });
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
