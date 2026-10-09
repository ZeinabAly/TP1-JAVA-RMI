package client.src.client.console;

import client.src.client.logic.*;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Interface console : menu, lecture et validation des saisies, affichage des résultats.
 * Aucun import de java.rmi : tout passe par la logique client.
 */
public class ConsoleUI {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CabinetClient logique;
    private final BufferedReader entree;
    private final PrintStream sortie = System.out;
    private final Object verrou = new Object();
    private volatile String invite = "> ";

    /** Fin du flux de saisie (Ctrl+D) : on quitte proprement. */
    private static final class FinDeSaisie extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

        FinDeSaisie() {
            super(null, null, false, false);
        }
    }

    public ConsoleUI(CabinetClient logique) {
        this.logique = logique;
        this.entree = new BufferedReader(new InputStreamReader(System.in));
    }

    public void demarrer() {
        sortie.println("=== Cabinet vétérinaire ===");
        try {
            boolean continuer = true;
            while (continuer) {
                afficherMenu();
                String choix = lire("> ").trim();
                try {
                    continuer = traiter(choix);
                } catch (ClientException e) {
                    sortie.println("Erreur : " + e.getMessage());
                }
            }
        } catch (FinDeSaisie e) {
            sortie.println();
            sortie.println("Fin de la saisie.");
        } finally {
            logique.close(); // désabonne et désexporte : sinon la JVM resterait en vie
            sortie.println("Au revoir.");
        }
    }

    private void afficherMenu() {
        sortie.println();
        sortie.println("1. Lister les patients");
        sortie.println("2. Rechercher un patient");
        sortie.println("3. Enregistrer un patient");
        sortie.println("4. Consulter le dossier d'un patient");
        sortie.println("5. Mettre à jour le dossier d'un patient");
        sortie.println("6. S'abonner aux alertes");
        sortie.println("7. Se désabonner des alertes");
        sortie.println("8. Supprimer un patient");
        sortie.println("0. Quitter");
    }

    private boolean traiter(String choix) throws ClientException {
        switch (choix) {
            case "1" -> lister();
            case "2" -> rechercher();
            case "3" -> enregistrer();
            case "4" -> consulterDossier();
            case "5" -> mettreAJourDossier();
            case "6" -> {
                logique.sAbonner(this::afficherAlerte);
                sortie.println("Abonné aux alertes.");
            }
            case "7" -> {
                logique.seDesabonner();
                sortie.println("Désabonné des alertes.");
            }
            case "8" -> supprimer();
            case "0" -> {
                return false;
            }
            default -> sortie.println("Choix invalide : tapez un numéro entre 0 et 8.");
        }
        return true;
    }

    // ------------------------------------------------------------------ fonctionnalités

    private void lister() throws ClientException {
        List<PatientVue> patients = logique.listerPatients();
        if (patients.isEmpty()) {
            sortie.println("Aucun patient.");
            return;
        }
        sortie.println(patients.size() + " patient(s) :");
        patients.forEach(p -> sortie.println("  " + formater(p)));
    }

    private void rechercher() throws ClientException {
        String nom = lireNonVide("Nom du patient : ");
        Optional<PatientVue> p = logique.rechercher(nom);
        if (p.isPresent()) {
            sortie.println("  " + formater(p.get()));
        } else {
            sortie.println("Aucun patient nommé '" + nom + "'.");
        }
    }

    private void enregistrer() throws ClientException {
        String nom = lireNonVide("Nom de l'animal : ");
        String maitre = lireNonVide("Nom du maître : ");
        String race = lireNonVide("Race : ");
        String espece = lireNonVide("Espèce : ");
        int vie = lireEntier("Espérance de vie moyenne (années) : ", 1, 150);
        PatientVue p = logique.enregistrer(nom, maitre, race, espece, vie);
        sortie.println("Patient enregistré : " + formater(p));
        sortie.println("Le cabinet compte maintenant " + logique.nombrePatients() + " patient(s).");
    }

    private void supprimer() throws ClientException {
        String nom = lireNonVide("Nom du patient à supprimer : ");
        sortie.println(logique.supprimer(nom) ? "Patient supprimé." : "Aucun patient nommé '" + nom + "'.");
    }

    private void consulterDossier() throws ClientException {
        String nom = lireNonVide("Nom du patient : ");
        DossierVue d = logique.consulterDossier(nom);
        sortie.println("Dossier de " + d.patient() + " - état de santé : " + d.etatSante());
        if (d.historique().isEmpty()) {
            sortie.println("  Aucune observation.");
        }
        for (ObservationVue o : d.historique()) {
            sortie.println("  - " + DATE.format(o.date()) + " : " + o.texte());
        }
    }

    private void mettreAJourDossier() throws ClientException {
        String nom = lireNonVide("Nom du patient : ");
        sortie.println("1. Modifier l'état de santé");
        sortie.println("2. Ajouter une observation");
        String sousChoix = lire("> ").trim();
        switch (sousChoix) {
            case "1" -> {
                logique.majEtatSante(nom, lireNonVide("Nouvel état de santé : "));
                sortie.println("État de santé mis à jour.");
            }
            case "2" -> {
                logique.ajouterObservation(nom, lireNonVide("Observation : "));
                sortie.println("Observation ajoutée.");
            }
            default -> sortie.println("Choix invalide.");
        }
    }

    // ------------------------------------------------------------------ alertes

    /**
     * Appelée sur un fil RMI, éventuellement pendant une saisie : on affiche l'alerte sur sa propre ligne,
     * puis on réaffiche l'invite en cours pour que l'utilisateur ne perde pas le fil.
     */
    private void afficherAlerte(String message) {
        synchronized (verrou) {
            sortie.println();
            sortie.println("[ALERTE] " + message);
            sortie.print(invite);
            sortie.flush();
        }
    }

    // ------------------------------------------------------------------ saisies

    private String lire(String question) {
        synchronized (verrou) {
            invite = question;
            sortie.print(question);
            sortie.flush();
        }
        try {
            String ligne = entree.readLine();
            if (ligne == null) throw new FinDeSaisie();
            return ligne;
        } catch (IOException e) {
            throw new FinDeSaisie();
        }
    }

    private String lireNonVide(String question) {
        while (true) {
            String s = lire(question).trim();
            if (!s.isEmpty()) return s;
            sortie.println("Saisie vide : veuillez réessayer.");
        }
    }

    private int lireEntier(String question, int min, int max) {
        while (true) {
            String s = lire(question).trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException e) {
                // traité ci-dessous
            }
            sortie.println("Saisie invalide : entrez un entier entre " + min + " et " + max + ".");
        }
    }

    private static String formater(PatientVue p) {
        return String.format("%s (maître : %s, race : %s, espèce : %s, %d ans)",
                p.nom(), p.maitre(), p.race(), p.espece(), p.esperanceVie());
    }
}
