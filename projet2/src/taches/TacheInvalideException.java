package taches;

/** Levée quand on tente de créer ou de manipuler une tâche invalide (titre vide, numéro hors liste...). */
public class TacheInvalideException extends Exception {

    public TacheInvalideException(String message) {
        super(message);
    }
}
