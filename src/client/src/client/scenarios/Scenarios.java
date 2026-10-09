package client.src.client.scenarios;

import common.src.common.*;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Scénarios de test des points de contrôle A, B, C, D, E et G (ce sont des tests, pas l'application).
 * Usage : Scenarios [hote] [port]. Le point F (deux clients abonnés) se vérifie avec deux CLI.
 */
public final class Scenarios {
    private Scenarios() {
    }

    public static void main(String[] args) throws Exception {
        String hote = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 1099;
        Registry registre = LocateRegistry.getRegistry(hote, port);
        CabinetVet cabinet = (CabinetVet) registre.lookup(CabinetVet.NOM_SERVICE);
        String suffixe = String.valueOf(System.currentTimeMillis() % 100000); //Ajouter les chiffres aleatoires pour éviter les doublons

        titre("Point de contrôle A : objet distant");
        System.out.println("Classe du stub : " + cabinet.getClass().getName());
        verifier(Proxy.isProxyClass(cabinet.getClass()), "le stub est un proxy dynamique");
        Animal boby = cabinet.rechercherParNom("Boby");
        verifier(boby != null, "patient 'Boby' trouvé : " + (boby != null ? boby.getNom() : "?"));
        if (boby == null) return;

        titre("Point de contrôle B : l'espèce est passée par valeur");
        Espece copie = boby.getEspece();
        int hashClient = System.identityHashCode(copie);
        int hashServeur = boby.identiteEspeceServeur();
        String original = copie.getNom();
        copie.setNom("MODIFIE-LOCALEMENT");
        System.out.println("identityHashCode client = " + hashClient + ", serveur = " + hashServeur);
        verifier(hashClient != hashServeur, "deux objets Java distincts");
        verifier(original.equals(boby.getEspece().getNom()), "le serveur renvoie toujours la valeur d'origine");

        titre("Point de contrôle C : le dossier est partagé");
        DossierSuivi d1 = cabinet.rechercherParNom("Boby").getDossier();
        DossierSuivi d2 = cabinet.rechercherParNom("Boby").getDossier();
        System.out.println("Classe du dossier : " + d1.getClass().getName());
        d1.setEtatSante("Sous traitement " + suffixe);
        verifier(d2.getEtatSante().equals("Sous traitement " + suffixe), "la modification est visible par une autre lecture");

        titre("Point de contrôle D : point d'entrée unique");
        verifier(registre.list().length == 1, "une seule liaison dans le registre : " + String.join(",", registre.list()));
        verifier(cabinet.rechercherParNom("Inconnu-" + suffixe) == null, "recherche infructueuse : null (documenté)");

        titre("Point de contrôle E : création d'un patient depuis le client");
        int avant = cabinet.getNombrePatients();
        cabinet.ajouterPatient("Rex-" + suffixe, "Paul", "Berger", new Espece("Chien", 12));
        verifier(cabinet.getNombrePatients() == avant + 1, "le nombre de patients est passé de " + avant + " à " + (avant + 1));
        verifier(cabinet.rechercherParNom("Rex-" + suffixe) != null, "le nouveau patient est retrouvé par son nom");

        titre("Point de contrôle G : ce qui traverse le réseau (A7)");
        try {
            cabinet.ajouterPatient("Medor-" + suffixe, "Lea", "Labrador", new ChienClientSeul(13));
            verifier(false, "un échec attendu avec une classe connue du client seul");
        } catch (RemoteException e) {
            System.out.println("Chaîne d'exceptions :");
            for (Throwable t = e; t != null; t = t.getCause()) {
                System.out.println("  -> " + t.getClass().getName() + " : " + t.getMessage());
            }
            Throwable racine = e;
            while (racine.getCause() != null) racine = racine.getCause();
            verifier(racine instanceof ClassNotFoundException, "cause profonde : ClassNotFoundException");
        }
        cabinet.ajouterPatient("Medor-" + suffixe, "Lea", "Labrador", new Chien(13));
        verifier(cabinet.rechercherParNom("Medor-" + suffixe) != null, "après correction (Chien dans common), le patient est ajouté");
    }

    private static void titre(String t) {
        System.out.println();
        System.out.println("=== " + t + " ===");
    }

    private static void verifier(boolean condition, String message) {
        System.out.println((condition ? "[OK]     " : "[ECHEC]  ") + message);
    }
}

