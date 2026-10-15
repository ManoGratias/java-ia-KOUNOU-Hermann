package chatbot;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Boucle de conversation en console. */
public class Main {

    /** Persona du bot (message système). */
    static final String MESSAGE_SYSTEME =
            "Tu es un tuteur Java bienveillant pour des étudiants de Bac+3. "
            + "Tu expliques les concepts avec des exemples courts, mais tu ne donnes jamais "
            + "la solution complète d'un exercice : tu guides avec des indices et des questions. "
            + "Réponds en français, de façon concise.";

    private static final List<String> conversation = new ArrayList<>();

    // Prédiction (avant de lancer) : sans AI_API_KEY => message "mode HORS-LIGNE" et réponses factices ;
    // avec une fausse clé => "[Erreur] Clé API invalide (401)" sans planter ; avec la vraie clé => vraie réponse,
    // et une 2e question qui fait référence à la 1re est comprise (historique). /quit quitte.
    // Résultat observé : conforme, sauf le modèle de l'énoncé (llama-3.1-8b-instant) qui renvoie 404 :
    // remplacé par openai/gpt-oss-20b avec l'accord de l'enseignant.
    public static void main(String[] args) {
        AiClient client = creerClient();
        Scanner scanner = new Scanner(System.in, encodageClavier());

        System.out.println("Pose ta question (/help pour les commandes).");
        while (true) {
            System.out.print("\nToi > ");
            if (!scanner.hasNextLine()) {
                break; // entrée fermée
            }
            String saisie = scanner.nextLine().trim();
            if (saisie.isEmpty()) {
                continue;
            }

            if (saisie.startsWith("/")) {
                if (saisie.equalsIgnoreCase("/quit")) {
                    break;
                }
                executerCommande(saisie.toLowerCase(), client);
                continue;
            }

            try {
                String reponse = client.ask(saisie);
                System.out.println("\nIA > " + reponse);
                conversation.add("Toi : " + saisie);
                conversation.add("IA : " + reponse);
            } catch (AiException e) {
                System.out.println("\n[Erreur] " + e.getMessage());
            }
        }
        System.out.println("Au revoir !");
    }

    /** Si AI_API_KEY existe, vraie IA ; sinon client factice, avec un message explicite. */
    private static AiClient creerClient() {
        String cle = System.getenv("AI_API_KEY");
        if (cle == null || cle.isBlank()) {
            System.out.println("Variable AI_API_KEY absente : mode HORS-LIGNE (MockAiClient, réponses factices).");
            return new MockAiClient();
        }
        System.out.println("Clé trouvée : connexion à Groq (" + HttpAiClient.modeleChoisi() + ").");
        return new HttpAiClient(cle.trim(), MESSAGE_SYSTEME);
    }

    private static void executerCommande(String commande, AiClient client) {
        switch (commande) {
            case "/help" -> {
                System.out.println("/help   affiche cette aide");
                System.out.println("/reset  efface l'historique (l'IA oublie la conversation)");
                System.out.println("/save   écrit la conversation dans un fichier texte");
                System.out.println("/quit   quitte le programme");
            }
            case "/reset" -> {
                client.reset();
                conversation.clear();
                System.out.println("Historique effacé.");
            }
            case "/save" -> sauvegarder();
            default -> System.out.println("Commande inconnue, tape /help.");
        }
    }

    private static void sauvegarder() {
        String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path fichier = Path.of("conversation-" + horodatage + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(fichier, StandardCharsets.UTF_8)) {
            for (String ligne : conversation) {
                writer.write(ligne);
                writer.newLine();
            }
            System.out.println("Conversation enregistrée dans " + fichier.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("[Erreur] Sauvegarde impossible : " + e.getMessage());
        }
    }

    /**
     * Encodage du clavier. Sous Windows, la console envoie les accents en cp850 alors que Java lit
     * en UTF-8 par défaut : sans ça, "Café" devient "Caf?". Hors console (entrée redirigée), on garde le défaut.
     */
    private static Charset encodageClavier() {
        Console console = System.console();
        return console != null ? console.charset() : Charset.defaultCharset();
    }
}
