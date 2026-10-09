package common.src.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/** Point d'entrée unique, seul objet publié dans le registre (A4). */
public interface CabinetVet extends Remote {

    /** Nom de la liaison dans le registre. */
    String NOM_SERVICE = "Cabinet";

    /* Retourne des stubs d'animaux (moyens de les joindre), pas des copies. */
    List<Animal> getPatients() throws RemoteException;

    /* Recherche par nom exact. Retourne null si aucun patient ne porte ce nom (comportement documenté). */
    Animal rechercherParNom(String nom) throws RemoteException;

    /*
     * Crée le patient CÔTÉ SERVEUR (A5) : seules des données traversent le réseau.
     * @throws IllegalArgumentException si un champ est vide ou si le nom existe déjà
     */
    Animal ajouterPatient(String nom, String nomMaitre, String race, Espece espece) throws RemoteException;

    /** Supprime un patient. Permet de franchir les seuils à la baisse (A6). @return false si le nom est inconnu. */
    boolean supprimerPatient(String nom) throws RemoteException;


    int getNombrePatients() throws RemoteException;

    // A6 - Observateur
    void abonner(AlerteObserver o) throws RemoteException;
    void desabonner(AlerteObserver o) throws RemoteException;

    int getNombreObservateurs() throws RemoteException;
}

