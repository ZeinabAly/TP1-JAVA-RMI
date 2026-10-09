package client;

import common.AlerteObserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// Chaque Observer (Observateur) représente un vétérinaire
public class AlerteObserverImpl extends UnicastRemoteObject implements AlerteObserver {
    public AlerteObserverImpl() throws RemoteException {
        super();
    }

    @Override
    public void notifier(String message){
        System.out.println("[ALERTE] " + message);
    }

}
