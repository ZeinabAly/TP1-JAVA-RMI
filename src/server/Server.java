package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

import common.*;


public class Server {
    public static final int PORT = 1099;

    //CREATION DE L'OBJET SEPARE
    /*public static Animal obj() throws RemoteException {
        Espece espece = new Espece("BullDog", 10);
        DossierSuivi dossier = new DosSuiviImpl("Bonne santé");
        dossier.ajouterObservation(new Consultation(LocalDate.of(2026, 10, 2).atStartOfDay(), "Consultation"));
        return new AnimalImpl("Boby", "Marc", "blanche", espece, dossier);
    }*/

    //Peupler Cabinet
    private static void peuplerCabinet(CabinetVetImpl cabinet) throws RemoteException {
        Espece espece1 = new Espece("BullDog", 10);
        DossierSuivi dossier1 = new DosSuiviImpl("Bonne santé");
        dossier1.ajouterObservation(new Consultation(LocalDate.of(2026, 10, 2).atStartOfDay(), "Consultation"));
        cabinet.ajouter(new AnimalImpl("Boby", "Marc", "BullDog", espece1, dossier1));

        // PATIENT 2
        Espece espece2 = new Espece("Caniche", 5);
        DossierSuivi dossier2 = new DosSuiviImpl("Bonne santé");
        dossier2.ajouterObservation(new Consultation(LocalDate.of(2026, 1, 2).atStartOfDay(), "Consultation"));
        cabinet.ajouter(new AnimalImpl("Marc", "Fred", "Caniche", espece2, dossier2));
    }

    public static CabinetVet obj() throws RemoteException {
        return new CabinetVetImpl();
    }

    public static void main(String[] args) {
        try {

            //Stocker obj() dans une variable de type Cabinet pour éviter de le récréer à chaque appel

            //Ajouter des patients
            CabinetVetImpl cabinet = new CabinetVetImpl();
            peuplerCabinet(cabinet);

            Registry registry = LocateRegistry.createRegistry(PORT);

            registry.rebind("Cabinet", cabinet);
            System.out.println("Server ready");

        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }


    }
}
