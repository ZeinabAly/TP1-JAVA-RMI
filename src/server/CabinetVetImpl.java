package server;

import common.Animal;
import common.CabinetVet;
import common.Espece;

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

    // Point de contrôle A5
    @Override
    public synchronized Animal ajouterPatient(String nom, String nomMaitre, String race, Espece espece) throws RemoteException{
        Animal a = new AnimalImpl(nom, nomMaitre, race, espece, new DosSuiviImpl("Non évalué"));
        animaux.add(a);
        return a;
    }

    @Override
    public int getNombrePatients(){
        return animaux.size();
    }
}
