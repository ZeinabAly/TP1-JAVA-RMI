package client.src.client;

import client.src.client.console.*;
import client.src.client.logic.*;

/**
 * Point d'entrée de la CLI : lit les arguments (hôte, port), obtient le stub, lance l'interface.
 * Usage : Main [hote=localhost] [port=1099]
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        String hote = args.length > 0 ? args[0] : "localhost";
        int port = 1099;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide : " + args[1] + ". Usage : Main [hote] [port]");
                System.exit(2);
            }
        }
        try (CabinetClient logique = Connexion.ouvrir(hote, port)) {
            new ConsoleUI(logique).demarrer();
        } catch (ClientException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
