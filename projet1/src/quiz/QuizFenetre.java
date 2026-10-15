package quiz;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Collections;
import java.util.List;

/**
 * Version fenêtrée (Swing) du quiz. Réutilise Question, Quiz.QUESTIONS, Quiz.selectionner()
 * et Quiz.messagePalier() : seule la façon d'afficher change.
 * Trois écrans empilés dans un CardLayout : accueil, question, résultat.
 */
public class QuizFenetre extends JFrame {

    private static final String ECRAN_ACCUEIL = "accueil";
    private static final String ECRAN_QUESTION = "question";
    private static final String ECRAN_RESULTAT = "resultat";

    private final CardLayout cartes = new CardLayout();
    private final JPanel ecrans = new JPanel(cartes);

    private final JComboBox<String> choixTheme = new JComboBox<>(new String[] {"Tout", "Java", "Informatique"});
    private final JComboBox<String> choixDifficulte = new JComboBox<>(new String[] {"Mélangée", "Facile", "Difficile"});

    private final JLabel progression = new JLabel();
    private final JLabel enonce = new JLabel();
    private final JTextField reponse = new JTextField(20);
    private final JLabel retour = new JLabel(" ");
    private final JButton valider = new JButton("Valider");

    private final JLabel resultatScore = new JLabel();
    private final JLabel resultatMessage = new JLabel();

    private List<Question> questions;
    private int indice;
    private int score;
    private long debut;
    private boolean enAttenteSuivante; // après une réponse, le bouton sert à passer à la question suivante

    public QuizFenetre() {
        super("Quiz Java");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Largeur de référence pour éviter que le texte des listes soit tronqué ("Mélan...")
        choixTheme.setPrototypeDisplayValue("Informatique   ");
        choixDifficulte.setPrototypeDisplayValue("Informatique   ");

        ecrans.add(enHaut(creerAccueil()), ECRAN_ACCUEIL);
        ecrans.add(enHaut(creerEcranQuestion()), ECRAN_QUESTION);
        ecrans.add(enHaut(creerEcranResultat()), ECRAN_RESULTAT);
        add(ecrans);

        setSize(560, 300);
        setLocationRelativeTo(null);
    }

    private JPanel creerAccueil() {
        JPanel panneau = colonne();
        panneau.add(titre("=== QUIZ ==="));
        panneau.add(ligne(new JLabel("Thème : "), choixTheme));
        panneau.add(ligne(new JLabel("Difficulté : "), choixDifficulte));
        JButton commencer = new JButton("Commencer");
        commencer.addActionListener(e -> commencer());
        panneau.add(ligne(commencer));
        return panneau;
    }

    private JPanel creerEcranQuestion() {
        JPanel panneau = colonne();
        enonce.setFont(enonce.getFont().deriveFont(Font.BOLD, 15f));
        panneau.add(ligne(progression));
        panneau.add(ligne(enonce));
        panneau.add(ligne(reponse, valider));
        panneau.add(ligne(retour));

        // Entrée dans le champ = clic sur le bouton
        reponse.addActionListener(e -> valider.doClick());
        valider.addActionListener(e -> {
            if (enAttenteSuivante) {
                questionSuivante();
            } else {
                verifier();
            }
        });
        return panneau;
    }

    private JPanel creerEcranResultat() {
        JPanel panneau = colonne();
        panneau.add(titre("=== RÉSULTAT ==="));
        panneau.add(ligne(resultatScore));
        panneau.add(ligne(resultatMessage));
        JButton rejouer = new JButton("Rejouer");
        rejouer.addActionListener(e -> cartes.show(ecrans, ECRAN_ACCUEIL));
        JButton quitter = new JButton("Quitter");
        quitter.addActionListener(e -> dispose());
        panneau.add(ligne(rejouer, quitter));
        return panneau;
    }

    private void commencer() {
        String theme = (String) choixTheme.getSelectedItem();
        int difficulte = choixDifficulte.getSelectedIndex(); // 0 = mélangée, 1 = facile, 2 = difficile
        questions = Quiz.selectionner(theme, difficulte);
        Collections.shuffle(questions);
        indice = 0;
        score = 0;
        debut = System.currentTimeMillis();

        if (questions.isEmpty()) {
            afficherResultat();
            return;
        }
        afficherQuestion();
        cartes.show(ecrans, ECRAN_QUESTION);
        // Le champ n'est visible qu'après le changement d'écran : on demande le focus juste après.
        SwingUtilities.invokeLater(reponse::requestFocusInWindow);
    }

    private void afficherQuestion() {
        Question q = questions.get(indice);
        progression.setText("Question " + (indice + 1) + "/" + questions.size() + "   -   Score : " + score);
        // Le HTML permet le retour à la ligne automatique dans un JLabel
        enonce.setText("<html><body style='width:400px'>" + q.getEnonce() + "</body></html>");
        reponse.setText("");
        reponse.setEditable(true);
        retour.setText(" ");
        valider.setText("Valider");
        enAttenteSuivante = false;
        reponse.requestFocusInWindow();
    }

    private void verifier() {
        Question q = questions.get(indice);
        String saisie = reponse.getText().trim();

        if (saisie.isEmpty()) {
            retour.setForeground(Color.GRAY);
            retour.setText("Pas de réponse. La bonne réponse était : " + q.getReponse());
        } else if (q.estCorrecte(saisie)) {
            score++;
            retour.setForeground(new Color(0, 130, 0));
            retour.setText("Bonne réponse !");
        } else {
            retour.setForeground(new Color(180, 0, 0));
            retour.setText("Raté. La bonne réponse était : " + q.getReponse());
        }

        reponse.setEditable(false);
        enAttenteSuivante = true;
        valider.setText(indice + 1 < questions.size() ? "Suivante" : "Voir le résultat");
        valider.requestFocusInWindow();
    }

    private void questionSuivante() {
        indice++;
        if (indice < questions.size()) {
            afficherQuestion();
        } else {
            afficherResultat();
        }
    }

    private void afficherResultat() {
        long secondes = (System.currentTimeMillis() - debut) / 1000;
        resultatScore.setText("Score : " + score + "/" + questions.size() + "   -   Temps : " + secondes + " s");
        resultatMessage.setText(Quiz.messagePalier(score, questions.size()));
        cartes.show(ecrans, ECRAN_RESULTAT);
    }

    // --- petites aides de mise en page ---

    private static JPanel colonne() {
        JPanel panneau = new JPanel();
        panneau.setLayout(new BoxLayout(panneau, BoxLayout.Y_AXIS));
        panneau.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panneau;
    }

    private static JPanel ligne(Component... composants) {
        JPanel ligne = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        for (Component c : composants) {
            ligne.add(c);
        }
        ligne.setAlignmentX(Component.LEFT_ALIGNMENT);
        return ligne;
    }

    private static JPanel titre(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        JPanel panneau = new JPanel(new BorderLayout());
        panneau.add(label, BorderLayout.WEST);
        panneau.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        panneau.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panneau;
    }

    /** Place l'écran en haut de la fenêtre : sinon BoxLayout étire les lignes sur toute la hauteur. */
    private static JPanel enHaut(JPanel ecran) {
        JPanel conteneur = new JPanel(new BorderLayout());
        conteneur.add(ecran, BorderLayout.NORTH);
        return conteneur;
    }

    public static void main(String[] args) {
        // Swing doit être manipulé depuis son propre thread (l'EDT) : invokeLater s'en charge.
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // pas grave : on garde l'apparence par défaut
            }
            new QuizFenetre().setVisible(true);
        });
    }
}
