package client.src.client.logic;

import java.util.List;

public record DossierVue(String patient, String etatSante, List<ObservationVue> historique) {
}
