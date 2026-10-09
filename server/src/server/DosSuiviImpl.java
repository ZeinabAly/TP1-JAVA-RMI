package server;

import common.DossierSuivi;
import common.Consultation;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class DosSuiviImpl extends UnicastRemoteObject implements DossierSuivi {
    private String etatSante;
    private List <Consultation> historique = new ArrayList<>() ;

    public DosSuiviImpl(String etatSante) throws RemoteException {
        super();
        this.etatSante = etatSante;
    }
    @Override
    public String getEtatSante() throws RemoteException {
        return etatSante;
    }
    @Override
    public void setEtatSante(String etatSante) throws RemoteException {
        this.etatSante = etatSante;
    }

    @Override
    public void ajouterObservation(Consultation obs) throws RemoteException {
        historique.add(obs);
    }

    @Override
    public List<Consultation> getHistorique() throws RemoteException{
        return new ArrayList<>(historique);
    }
}
