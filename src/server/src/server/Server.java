package server.src.server;

import common.src.common.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Lanceur du serveur : registre interne (createRegistry) + publication du seul cabinet.
 * Usage : Server [port=1099] [nombreDePatientsInitiaux=2]
 * (pour tester les seuils : "Server 1099 99", puis ajouter un patient depuis un client)
 */
public class Server {
    public static final int PORT_PAR_DEFAUT = 1099;

    // Références statiques : un servant exporté n'est retenu que faiblement par le runtime RMI,
    // sans référence forte il pourrait être ramassé par le GC quand plus aucun client ne le référence.
    private static CabinetVetImpl cabinet;
    private static Registry registre;

    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT_PAR_DEFAUT;
            int total = args.length > 1 ? Integer.parseInt(args[1]) : 2;

            cabinet = new CabinetVetImpl();
            peupler(cabinet, total);

            registre = LocateRegistry.createRegistry(port);
            registre.rebind(CabinetVet.NOM_SERVICE, cabinet);

            System.out.println("Serveur prêt : service '" + CabinetVet.NOM_SERVICE + "' publié sur le port " + port
                    + " (" + cabinet.getNombrePatients() + " patients).");
        } catch (NumberFormatException e) {
            System.err.println("Usage : Server [port] [nombreDePatientsInitiaux]");
            System.exit(2);
        } catch (RemoteException e) {
            System.err.println("Impossible de démarrer le serveur (port déjà utilisé ?) : " + e.getMessage());
            System.exit(1);
        }
    }

    private static void peupler(CabinetVetImpl c, int total) throws RemoteException {
        for (int i = 1; i <= total; i++) {
            switch (i) {
                case 1 -> creer(c, "Boby", "Marc", "BullDog", new Espece("Chien", 10), "Bonne santé");
                case 2 -> creer(c, "Marc", "Fred", "Caniche", new Espece("Chien", 14), "Bonne santé");
                default -> creer(c, "Patient-" + i, "Maitre-" + i, "Inconnue", new Espece("Chien", 12), "Non évalué");
            }
        }
    }

    private static void creer(CabinetVetImpl c, String nom, String maitre, String race, Espece espece, String etat)
            throws RemoteException {
        DosSuiviImpl dossier = new DosSuiviImpl(etat);
        dossier.ajouterObservation(new Consultation(LocalDateTime.now().minusDays(30), "Première consultation"));
        c.ajouterInitial(new AnimalImpl(nom, maitre, race, espece, dossier));
    }
}

