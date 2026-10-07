package common;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Animal extends Remote{
    public void infosAnimal() throws RemoteException;
    public Espece getEspece() throws RemoteException;
    public String getNom() throws RemoteException;
    public String getNomMaitre() throws RemoteException;
    public String getRace() throws RemoteException;
    public DossierSuivi getDossier() throws RemoteException;
}
