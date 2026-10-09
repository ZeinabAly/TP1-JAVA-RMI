package common.src.common;

import java.rmi.Remote;
import java.rmi.RemoteException;


/** Patient du cabinet : objet distant (il reste dans la JVM du serveur). */
public interface Animal extends Remote {
    String getNom() throws RemoteException;
    String getNomMaitre() throws RemoteException;
    String getRace() throws RemoteException;

    /** Retourne une COPIE de l'espèce (passage par valeur). */
    Espece getEspece() throws RemoteException;

    /** Retourne un stub : le dossier reste sur le serveur (passage par référence). */
    DossierSuivi getDossier() throws RemoteException;

    /** Résumé textuel calculé côté serveur. */
    String infosAnimal() throws RemoteException;

    /** Outil d'observation du point de contrôle B : identityHashCode de l'espèce DANS la JVM du serveur. */
    int identiteEspeceServeur() throws RemoteException;
}