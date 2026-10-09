package server;

import common.AlerteObserver;
import common.Animal;
import common.CabinetVet;
import common.Espece;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CabinetVetImpl extends UnicastRemoteObject implements CabinetVet {
    private List<Animal> animaux = new ArrayList<>();
    // Créer une liste d'observateurs pour gérer l'abonnement, le désabonnement et la diffusion
    private List<AlerteObserver> veterinaires = new CopyOnWriteArrayList<>();
    //CopyOnWriteArrayList contrairement à ArrayList, permet de travailler sur copie même lorsqu'une boucle est en train de tourner
    //Ainsi le serveur peut être en train de faire une diffuser sur veterinaires pendant qu'un autre s'abonne

    private static final int[] SEUILS = {100, 500, 1000};

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
    public int getNombrePatients() throws RemoteException{
        return animaux.size();
    }

    @Override
    public void abonner(AlerteObserver o) throws RemoteException {
        veterinaires.add(o);
    }

    @Override
    public void desabonner(AlerteObserver o) throws RemoteException {
        veterinaires.remove(o);
    }

    // Ajouter la verification des seuils, le client n'a pas besoin d'y avoir accès
    private void verifierSeuils(int avant, int apres){
        for(int seuil: SEUILS){
            boolean monte = avant < seuil && apres >= seuil;
            boolean descend = avant >= seuil && apres < seuil;

            if(monte || descend) diffuser("Seuil" + seuil + (monte ? " franchi à la hausse " : " franchi à la baisse ") );
        }
    }

    private void diffuser(String msg){
        for (AlerteObserver vet: veterinaires){
            // Pour la robustesse du systeme, un client manquant ne doit pas empêcher l'envoi de message aux autres, on le retire juste de la liste.
            try{
                vet.notifier(msg);
            }catch (RemoteException e){
                veterinaires.remove(vet);
            }
        }
    }

    public int getNombreObservateurs() throws RemoteException{
        return veterinaires.size();
    }
}
