package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

import common.Animal;
import common.DossierSuivi;
import common.Espece;
import common.Consultation;


public class Server {
    public static final int PORT = 1099;

    //CREATION DE L'OBJET SEPARE
    public static Animal obj() throws RemoteException {
        Espece espece = new Espece("BullDog", 10);
        DossierSuivi dossier = new DosSuiviImpl("Bonne santé");
        dossier.ajouterObservation(new Consultation(LocalDate.of(2026, 10, 2).atStartOfDay(), "Consultation"));
        return new AnimalImpl("Boby", "Marc", "blanche", espece, dossier);
    }

    public static void main(String[] args) {
        try {

            //Stocker obj() dans une variable de type Animal pour éviter de le récréer à chaque appel
            Animal animal = obj();
            Registry registry = LocateRegistry.createRegistry(PORT);

            registry.rebind("Animal", animal);
            System.out.println("Server ready");
            //Afficher le code de l'espece
            System.out.println(System.identityHashCode(animal.getEspece()));
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }


    }
}
