package common.src.common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Consultation implements Serializable {
    private LocalDateTime date;
    private String texte;

    public Consultation(LocalDateTime date, String texte){
        this.date = date;
        this.texte = texte;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }
}
