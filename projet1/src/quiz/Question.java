package quiz;

/**
 * Une question du quiz : un énoncé, la réponse attendue, un thème et une difficulté.
 * La classe est le "moule" ; chaque new Question(...) crée un objet (une question précise).
 */
public class Question {

    private final String enonce;
    private final String reponse;
    private final String theme;
    private final int difficulte; // 1 = facile, 2 = difficile

    public Question(String enonce, String reponse, String theme, int difficulte) {
        this.enonce = enonce;
        this.reponse = reponse;
        this.theme = theme;
        this.difficulte = difficulte;
    }

    public String getEnonce() {
        return enonce;
    }

    public String getReponse() {
        return reponse;
    }

    public String getTheme() {
        return theme;
    }

    public int getDifficulte() {
        return difficulte;
    }

    /**
     * Compare la saisie à la réponse attendue en ignorant les espaces autour et la casse.
     * On utilise equalsIgnoreCase() et pas == : == compare les références (les adresses
     * en mémoire), pas le contenu des chaînes.
     */
    public boolean estCorrecte(String saisie) {
        if (saisie == null) {
            return false;
        }
        return saisie.trim().equalsIgnoreCase(reponse.trim());
    }
}
