package client;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.List;

import common.*;

public class Client {

    /// QUELQUES TEST SUR LES ANIMAUX - A1 à A3
    public void testsAnimalTrouve(Animal animal) throws RuntimeException, RemoteException {
        //Invocation de quelques méthodes
        System.out.println(animal.getNom());
        System.out.println(animal.getNomMaitre());
        System.out.println(animal.getRace());
        Espece e1 = animal.getEspece();
        System.out.println(System.identityHashCode(e1)); //Afficher le code
        System.out.println(e1.getNom()); //Récupérer le nom de e1
        e1.setNom("Chien"); //Modifier le nom
        System.out.println("e1 local après modif : " + e1.getNom()); //Affiche Chien
        Espece e2 = animal.getEspece(); //Vérifier si les changements s'appliquent
        System.out.println("Nom e2 redemandé : " + e2.getNom()); //Affiche le nom original publié sur le serveur

        //AJOUTER POUR TEST
        DossierSuivi d = animal.getDossier();
        System.out.println("Dossier : " + d.getClass().getName());   // $Proxy => distant
        System.out.println("Etat : " + d.getEtatSante());

        d.setEtatSante("Sous traitement");
        d.ajouterObservation(new Consultation(LocalDate.now().atStartOfDay(), "Vaccin"));

        // Relecture en repartant de l'animal : la modification est visible
        DossierSuivi d2 = animal.getDossier();
        System.out.println("Etat apres modif : " + d2.getEtatSante());
        d2.getHistorique().forEach(System.out::println);
    }

    //Afficher la liste des PATIENTS
    public void afficherPatients(CabinetVet stub) throws RemoteException{
        List<Animal> patients = stub.getPatients();
        for (Animal a: patients){
            System.out.println("Nom : "+a.getNom());
        }
    }

    public static void main(String[] args) {
        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            CabinetVet stub = (CabinetVet) registry.lookup("Cabinet");

            // Point de contrôle D : registry ne contient qu'une seule liaison
            System.out.println(registry.list().length);


            System.out.println("Classe du stub :" + stub.getClass().getName());
            System.out.println("Proxy dynamique ? " + Proxy.isProxyClass(stub.getClass()));

            // Liste des patients
            for(Animal a: stub.getPatients()){
                System.out.println(a.getNom());
            }

            // Rechercher un patient par nom
            System.out.println("Le patient recherché est : " + stub.rechercherParNom("Marc").getNom());

            // Rechercher un patient qui n'existe pas
            System.out.println(stub.rechercherParNom("Jean").getNom());

        } catch (Exception e) {
            System.out.println("Erreur");
            e.printStackTrace();
        }
    }
}
