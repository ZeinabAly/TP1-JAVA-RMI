package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CabinetVet extends Remote {
    List<Animal> getPatients() throws RemoteException;
    Animal rechercherParNom(String nom) throws RemoteException;

    // A5 - Le client peut envoyer les données pour créer un patient
    Animal ajouterPatient(String nom, String nomMaitre, String race, Espece espece) throws RemoteException;
    int getNombrePatients() throws RemoteException;

    // A6 - Observateur
    void abonner(AlerteObserver o) throws RemoteException;
    void desabonner(AlerteObserver o) throws RemoteException;

    int getNombreObservateurs() throws RemoteException;
}

