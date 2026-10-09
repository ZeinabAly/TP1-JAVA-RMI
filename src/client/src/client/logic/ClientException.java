package client.src.client.logic;

import java.io.Serial;

/** Erreur de la logique client, avec un message destiné à l'utilisateur (aucun type RMI exposé à l'interface). */
public class ClientException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
