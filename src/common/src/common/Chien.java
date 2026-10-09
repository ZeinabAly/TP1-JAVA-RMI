package common.src.common;

import java.io.Serial;

/**
 * Correction de l'expérience A7 : la sous-classe d'Espece est déplacée dans "common",
 * pour que le serveur puisse la charger lors de la désérialisation.
 */
public class Chien extends Espece {
    @Serial
    private static final long serialVersionUID = 1L;

    public Chien(int esperanceVie) {
        super("Chien", esperanceVie);
    }
}
