package common;

import java.io.Serial;
import java.io.Serializable;

public class Espece implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String nom;
    private int esperanceVie; //en année

    public Espece(String nom, int esperanceVie){
        this.nom = nom;
        this.esperanceVie = esperanceVie;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public int getEsperanceVie() { return esperanceVie; }
    public void setEsperanceVie(int esperanceVie) { this.esperanceVie = esperanceVie; }
}
