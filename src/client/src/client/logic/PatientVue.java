package client.src.client.logic;

/** Vue d'un patient prête à afficher (copie locale, aucune référence distante). */
public record PatientVue(String nom, String maitre, String race, String espece, int esperanceVie) {
}
