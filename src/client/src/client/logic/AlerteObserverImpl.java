package client.src.client.logic;

import common.src.common.AlerteObserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

// Chaque Observer (Observateur) représente un vétérinaire
public class AlerteObserverImpl extends UnicastRemoteObject implements AlerteObserver {

    private static final long serialVersionUID = 1L;

    private final transient Consumer<String> ecouteur;

    AlerteObserverImpl(Consumer<String> ecouteur) throws RemoteException {
        super();
        this.ecouteur = ecouteur;
    }

    @Override
    public void notifier(String message) {
        try {
            ecouteur.accept(message);
        } catch (RuntimeException e) {
            // Une erreur d'affichage ne doit jamais remonter au serveur.
        }
    }
}
