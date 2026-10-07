package server;

import common.Animal;
import common.CabinetVet;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class CabinetVetImpl implements CabinetVet {
    private List<Animal> animaux = new ArrayList<>();

    public CabinetVetImpl(){
        super();
    }

    @Override
    public List<Animal> getListPatients(){
        return animaux;
    }

    @Override
    public Animal rechercherParNom(String nom) throws RemoteException {
        for (Animal animal: animaux){
            if (nom.equals(animal.getNom())){ //Commencer par nom pour le cas où getNom est null
                return animal;
            }
        }
        return null;
    }
}
