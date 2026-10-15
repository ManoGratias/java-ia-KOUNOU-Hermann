package chatbot;

/** Erreur lors d'un appel à l'IA, avec un message lisible par l'utilisateur. */
public class AiException extends Exception {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
