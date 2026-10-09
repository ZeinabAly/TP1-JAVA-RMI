package client.src.client.scenarios;

import common.src.common.Espece;

import java.io.Serial;

/**
 * Sous-classe d'Espece définie dans le projet CLIENT uniquement (expérience A7).
 * Le serveur ne la connaît pas : sa désérialisation échoue avec une ClassNotFoundException.
 */
public class ChienClientSeul extends Espece {
    @Serial
    private static final long serialVersionUID = 1L;

    public ChienClientSeul(int esperanceVie) {
        super("Chien", esperanceVie);
    }
}

