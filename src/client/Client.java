package client;

import java.lang.reflect.Proxy;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

import common.Animal;
import common.DossierSuivi;
import common.Espece;
import common.Consultation;

public class Client {
    public static void main(String[] args) {
        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            Animal stub = (Animal) registry.lookup("Animal");

            System.out.println("Classe du stub :" + stub.getClass().getName());
            System.out.println("Proxy dynamique ? " + Proxy.isProxyClass(stub.getClass()));



            //Invocation de quelques méthodes
            System.out.println(stub.getNom());
            System.out.println(stub.getNomMaitre());
            System.out.println(stub.getRace());
            Espece e1 = stub.getEspece();
            System.out.println(System.identityHashCode(e1)); //Afficher le code
            System.out.println(e1.getNom()); //Récupérer le nom de e1
            e1.setNom("Chien"); //Modifier le nom
            System.out.println("e1 local après modif : " + e1.getNom()); //Affiche Chien
            Espece e2 = stub.getEspece(); //Vérifier si les changements s'appliquent
            System.out.println("Nom e2 redemandé : " + e2.getNom()); //Affiche le nom original publié sur le serveur

            //AJOUTER POUR TEST
            DossierSuivi d = stub.getDossier();
            System.out.println("Dossier : " + d.getClass().getName());   // $Proxy => distant
            System.out.println("Etat : " + d.getEtatSante());

            d.setEtatSante("Sous traitement");
            d.ajouterObservation(new Consultation(LocalDate.now().atStartOfDay(), "Vaccin"));

            // Relecture en repartant de l'animal : la modification est visible
            DossierSuivi d2 = stub.getDossier();
            System.out.println("Etat apres modif : " + d2.getEtatSante());
            d2.getHistorique().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Erreur");
            e.printStackTrace();
        }
    }
}
