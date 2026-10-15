package taches;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Partie "affichage" : le menu console. Toute la logique est dans GestionnaireTaches,
 * la lecture/écriture du fichier dans FichierTaches.
 */
public class Main {

    private static final Path FICHIER = Path.of("taches.txt");

    private static final GestionnaireTaches gestionnaire = new GestionnaireTaches();
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean entreeFermee = false;

    public static void main(String[] args) {
        FichierTaches fichier = new FichierTaches(FICHIER);
        charger(fichier);

        boolean continuer = true;
        while (continuer) {
            afficherMenu();
            String choix = lire("Ton choix : ");
            if (entreeFermee) {
                break; // Ctrl+Z / fin de fichier : on quitte proprement en sauvegardant
            }
            switch (choix) {
                case "1" -> ajouter();
                case "2" -> lister(gestionnaire.lister());
                case "3" -> marquerFaite();
                case "4" -> supprimer();
                case "5" -> supprimerTerminees();
                case "6" -> trier();
                case "7" -> enRetard();
                case "0" -> continuer = false;
                default -> System.out.println("Choix inconnu.");
            }
        }

        sauvegarder(fichier);
        System.out.println("Au revoir !");
    }

    private static void afficherMenu() {
        System.out.println();
        System.out.println("=== MES TÂCHES (" + gestionnaire.nombre() + ") ===");
        System.out.println("1) Ajouter");
        System.out.println("2) Lister");
        System.out.println("3) Marquer comme faite");
        System.out.println("4) Supprimer");
        System.out.println("5) Supprimer toutes les tâches terminées");
        System.out.println("6) Trier (priorité ou date)");
        System.out.println("7) Tâches en retard");
        System.out.println("0) Quitter (et sauvegarder)");
    }

    /** Lit une ligne ; renvoie "" si l'entrée clavier est fermée, au lieu de lever une exception. */
    private static String lire(String invite) {
        System.out.print(invite);
        if (!scanner.hasNextLine()) {
            entreeFermee = true;
            return "";
        }
        return scanner.nextLine().trim();
    }

    private static void charger(FichierTaches fichier) {
        try {
            for (Tache t : fichier.charger()) {
                gestionnaire.ajouter(t);
            }
            for (String avertissement : fichier.getAvertissements()) {
                System.out.println("Attention : " + avertissement);
            }
            System.out.println(gestionnaire.nombre() + " tâche(s) chargée(s).");
        } catch (IOException e) {
            System.out.println("Impossible de lire " + FICHIER + " : " + e.getMessage());
        }
    }

    private static void sauvegarder(FichierTaches fichier) {
        try {
            fichier.sauvegarder(gestionnaire.lister());
            System.out.println("Tâches sauvegardées dans " + FICHIER.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Erreur de sauvegarde : " + e.getMessage());
        }
    }

    private static void ajouter() {
        String titre = lire("Titre : ");
        Priorite priorite = lirePriorite();
        try {
            LocalDate echeance = GestionnaireTaches.parserDate(lire("Échéance AAAA-MM-JJ (vide = aucune) : "));
            gestionnaire.ajouter(new Tache(titre, priorite, echeance));
            System.out.println("Tâche ajoutée.");
        } catch (TacheInvalideException e) {
            System.out.println("Tâche non ajoutée : " + e.getMessage());
        }
    }

    private static Priorite lirePriorite() {
        String saisie = lire("Priorité (1 = basse, 2 = moyenne, 3 = haute) [2] : ");
        return switch (saisie) {
            case "1" -> Priorite.BASSE;
            case "3" -> Priorite.HAUTE;
            default -> Priorite.MOYENNE;
        };
    }

    private static void lister(List<Tache> taches) {
        if (taches.isEmpty()) {
            System.out.println("Aucune tâche.");
            return;
        }
        for (int i = 0; i < taches.size(); i++) {
            System.out.println((i + 1) + ". " + taches.get(i));
        }
    }

    private static void marquerFaite() {
        try {
            int numero = gestionnaire.parserNumero(lire("Numéro de la tâche faite : "));
            gestionnaire.marquerFaite(numero);
            System.out.println("Tâche n°" + numero + " marquée comme faite.");
        } catch (TacheInvalideException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void supprimer() {
        try {
            int numero = gestionnaire.parserNumero(lire("Numéro de la tâche à supprimer : "));
            Tache supprimee = gestionnaire.supprimer(numero);
            System.out.println("Supprimée : " + supprimee.getTitre());
        } catch (TacheInvalideException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void supprimerTerminees() {
        int nombre = gestionnaire.supprimerTerminees();
        System.out.println(nombre + " tâche(s) terminée(s) supprimée(s).");
    }

    private static void trier() {
        String choix = lire("Trier par 1) priorité ou 2) date : ");
        if (choix.equals("2")) {
            gestionnaire.trierParDate();
        } else {
            gestionnaire.trierParPriorite();
        }
        lister(gestionnaire.lister());
    }

    private static void enRetard() {
        List<Tache> retard = gestionnaire.enRetard(LocalDate.now());
        System.out.println(retard.size() + " tâche(s) en retard :");
        lister(retard);
    }
}
