package chatbot;

/**
 * Le "contrat" d'un client d'IA : n'importe quelle classe qui sait répondre à une question.
 * Main ne connaît que cette interface ; on peut brancher MockAiClient ou HttpAiClient sans rien changer d'autre.
 */
public interface AiClient {

    /** Envoie la question et renvoie le texte de la réponse. */
    String ask(String question) throws AiException;

    /** Oublie l'historique de la conversation. Par défaut : rien à oublier. */
    default void reset() {
    }
}
