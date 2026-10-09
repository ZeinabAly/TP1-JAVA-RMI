package common.src.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Question A6
public interface AlerteObserver extends Remote {
    void notifier(String message) throws RemoteException;
}
