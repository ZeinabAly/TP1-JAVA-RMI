package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DossierSuivi extends Remote {
    public void ajouterObservation(Consultation obs) throws RemoteException;
    public String getEtatSante() throws RemoteException;
    public void setEtatSante(String etatSante) throws RemoteException;
    public List<Consultation> getHistorique() throws RemoteException;

}
