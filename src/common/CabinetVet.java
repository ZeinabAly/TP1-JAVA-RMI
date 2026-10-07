package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CabinetVet extends Remote {
    public List<Animal> getPatients() throws RemoteException;
    public Animal rechercherParNom(String nom) throws RemoteException;
}
