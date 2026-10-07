package server;

import common.Animal;
import common.CabinetVet;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class CabinetVetImpl extends UnicastRemoteObject implements CabinetVet {
    private List<Animal> animaux = new ArrayList<>();

    public CabinetVetImpl() throws RemoteException{
        super();
    }

    public synchronized void ajouter(Animal a) {     // usage serveur seulement
        animaux.add(a);
    }

    @Override
    public List<Animal> getPatients(){
        return animaux;
    }

    @Override
    public Animal rechercherParNom(String nom) throws RemoteException {
        for (Animal animal: animaux){
            if (animal.getNom().equals(nom)){
                return animal;
            }
        }
        return null; //Null si aucun patient de ce nom
    }
}
