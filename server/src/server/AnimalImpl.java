package server;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import common.Animal;
import common.DossierSuivi;
import common.Espece;

public class AnimalImpl extends UnicastRemoteObject implements Animal{
    @Serial
    private static final long serialVersionUID = 1L;
    private String nom;
    private String nomMaitre;
    private String race;
    private Espece espece;
    private DossierSuivi dossierSuivi;
    public AnimalImpl(String nom, String nomMaitre, String race, Espece espece, DossierSuivi dossierSuivi) throws RemoteException{
        super();
        this.nom = nom;
        this.nomMaitre = nomMaitre;
        this.race = race;
        this.espece = espece;
        this.dossierSuivi = dossierSuivi;
    }

    @Override 
    public void infosAnimal() throws RemoteException{
        System.out.println("Nom : " + nom + 
        "Nom Maitre : " + nomMaitre + 
        "Race : "+race + 
        "Espèce : " + espece.getNom());
    }

    //Modifier la copie locale de Espece et la redemander
    @Override
    public Espece getEspece() throws RemoteException{
        return this.espece;
    }

    @Override
    public String getNom() throws RemoteException{
        return nom;
    }

    @Override
    public String getNomMaitre() throws RemoteException{
        return nomMaitre;
    }

    @Override
    public String getRace() throws RemoteException{
        return race;
    }

    @Override
    public DossierSuivi getDossier() throws RemoteException {
        return dossierSuivi;
    }
}
