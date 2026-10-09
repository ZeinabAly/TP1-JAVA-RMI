package client.src.client.logic;

import common.src.common.*;

import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** Obtention du stub du cabinet (lookup) : seul endroit du client qui parle au registre. */
public final class Connexion {
    private Connexion() {
    }

    public static CabinetClient ouvrir(String hote, int port) throws ClientException {
        try {
            Registry registre = LocateRegistry.getRegistry(hote, port);
            CabinetVet stub = (CabinetVet) registre.lookup(CabinetVet.NOM_SERVICE);
            return new CabinetClient(stub);
        } catch (NotBoundException e) {
            throw new ClientException("Le service '" + CabinetVet.NOM_SERVICE + "' n'est pas publié dans le registre "
                    + hote + ":" + port + ".", e);
        } catch (ConnectException | ConnectIOException e) {
            throw new ClientException("Aucun registre ne répond sur " + hote + ":" + port
                    + ". Le serveur est-il démarré ?", e);
        } catch (RemoteException e) {
            throw new ClientException("Connexion au registre impossible : " + e.getMessage(), e);
        }
    }
}
