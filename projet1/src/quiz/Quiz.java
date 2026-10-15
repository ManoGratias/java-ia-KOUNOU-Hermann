package quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Quiz en console.
 * Socle : questions posées une par une, comparaison tolérante, score avec paliers.
 * Extensions : rejouer (do...while), ordre aléatoire, choix du thème/difficulté, chronomètre.
 */
public class Quiz {

    // Tableau de questions (au moins 5 ; la 6e a été ajoutée pour le défi "sans IA").
    private static final Question[] QUESTIONS = {
        new Question("Quel mot-clé Java permet de créer un objet ?", "new", "Java", 1),
        new Question("Quel type Java stocke un nombre entier sur 32 bits ?", "int", "Java", 1),
        new Question("Quelle méthode est le point d'entrée d'un programme Java ?", "main", "Java", 1),
        new Question("Quel mot-clé permet à une classe d'hériter d'une autre ?", "extends", "Java", 2),
        new Question("Quelle classe sert à lire le clavier dans le cours ?", "Scanner", "Java", 1),
        new Question("Quel mot-clé empêche de modifier une variable après son initialisation ?", "final", "Java", 2),
        new Question("Combien de bits y a-t-il dans un octet ?", "8", "Informatique", 1),
        new Question("Quel langage sert à interroger une base de données relationnelle ?", "SQL", "Informatique", 1),
        new Question("En binaire, combien vaut 1010 en décimal ?", "10", "Informatique", 2),
        new Question("Quel protocole sert à afficher les pages web (sigle) ?", "HTTP", "Informatique", 1),
        new Question("Quelle est la base du système hexadécimal ?", "16", "Informatique", 2),
        new Question("Que signifie le I de IA ?", "intelligence", "Informatique", 1),
    };

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String rejouer;

        do {
            String theme = choisirTheme(scanner);
            int difficulte = choisirDifficulte(scanner);
            List<Question> selection = selectionner(theme, difficulte);
            Collections.shuffle(selection); // ordre aléatoire

            long debut = System.currentTimeMillis();
            int score = jouer(scanner, selection);
            long dureeSecondes = (System.currentTimeMillis() - debut) / 1000;

            afficherResultat(score, selection.size(), dureeSecondes);

            System.out.print("\nRejouer ? (o/n) : ");
            rejouer = lireLigne(scanner);
        } while (rejouer.equalsIgnoreCase("o") || rejouer.equalsIgnoreCase("oui"));

        System.out.println("À bientôt !");
    }

    /** Lit une ligne ; renvoie "" si l'entrée est fermée (Ctrl+Z / fin de fichier) au lieu de planter. */
    private static String lireLigne(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    private static String choisirTheme(Scanner scanner) {
        System.out.println("=== QUIZ ===");
        System.out.println("Thème : 1) Java   2) Informatique   3) Tout");
        System.out.print("Ton choix : ");
        String choix = lireLigne(scanner);
        if (choix.equals("1")) {
            return "Java";
        }
        if (choix.equals("2")) {
            return "Informatique";
        }
        return "Tout"; // saisie vide ou invalide : on prend tout
    }

    private static int choisirDifficulte(Scanner scanner) {
        System.out.println("Difficulté : 1) Facile   2) Difficile   3) Mélangée");
        System.out.print("Ton choix : ");
        String choix = lireLigne(scanner);
        if (choix.equals("1")) {
            return 1;
        }
        if (choix.equals("2")) {
            return 2;
        }
        return 0; // 0 = toutes les difficultés
    }

    private static List<Question> selectionner(String theme, int difficulte) {
        List<Question> selection = new ArrayList<>();
        // Boucle de 0 à length - 1 : aller jusqu'à l'indice length lèverait
        // une ArrayIndexOutOfBoundsException.
        for (int i = 0; i < QUESTIONS.length; i++) {
            Question q = QUESTIONS[i];
            boolean bonTheme = theme.equals("Tout") || q.getTheme().equals(theme);
            boolean bonneDifficulte = difficulte == 0 || q.getDifficulte() == difficulte;
            if (bonTheme && bonneDifficulte) {
                selection.add(q);
            }
        }
        return selection;
    }

    private static int jouer(Scanner scanner, List<Question> questions) {
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("\nQuestion " + (i + 1) + "/" + questions.size() + " : " + q.getEnonce());
            System.out.print("> ");
            String saisie = lireLigne(scanner);

            if (saisie.isEmpty()) {
                System.out.println("Pas de réponse. La bonne réponse était : " + q.getReponse());
            } else if (q.estCorrecte(saisie)) {
                System.out.println("Bonne réponse !");
                score++;
            } else {
                System.out.println("Raté. La bonne réponse était : " + q.getReponse());
            }
        }
        return score;
    }

    private static void afficherResultat(int score, int total, long dureeSecondes) {
        System.out.println("\n=== RÉSULTAT ===");
        System.out.println("Score : " + score + "/" + total);
        System.out.println("Temps total : " + dureeSecondes + " s");

        if (total == 0) {
            System.out.println("Aucune question pour ce choix.");
            return;
        }
        double pourcentage = score * 100.0 / total;
        if (pourcentage == 100) {
            System.out.println("Parfait ! Sans faute.");
        } else if (pourcentage >= 75) {
            System.out.println("Très bien, tu maîtrises le sujet.");
        } else if (pourcentage >= 50) {
            System.out.println("Pas mal, encore un petit effort.");
        } else if (pourcentage > 0) {
            System.out.println("Il faut réviser un peu...");
        } else {
            System.out.println("Zéro... on recommence ?");
        }
    }
}
