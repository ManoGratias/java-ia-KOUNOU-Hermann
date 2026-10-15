package quiz;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Version fenêtrée du quiz, thème sombre animé.
 * La logique reste dans Question et Quiz (QUESTIONS, selectionner(), messagePalier()) :
 * cette classe ne fait que l'affichage. Trois écrans dans un CardLayout : accueil, question, résultat.
 */
public class QuizFenetre extends Ui.Fenetre {

    private static final String ACCUEIL = "accueil";
    private static final String QUESTION = "question";
    private static final String RESULTAT = "resultat";
    private static final String[] THEMES = {"Tout", "Java", "Informatique"};

    private final CardLayout cartes = new CardLayout();
    private final JPanel ecrans = new JPanel(cartes);

    // Accueil
    private final Ui.Chips choixTheme = new Ui.Chips(THEMES);
    private final Ui.Chips choixDifficulte = new Ui.Chips("Mélangée", "Facile", "Difficile");
    private final JLabel nombreQuestions = Ui.texte("", 13, Font.PLAIN, Ui.DISCRET);
    private final Ui.Bouton commencer = new Ui.Bouton("Commencer  →", Ui.VIOLET, Ui.ROSE);

    // Question
    private final JLabel progression = Ui.texte("", 15, Font.BOLD, Ui.TEXTE);
    private final Ui.Pastille pastilleScore = new Ui.Pastille("Score 0", Ui.VIOLET);
    private final Ui.Pastille pastilleChrono = new Ui.Pastille("0:00", Ui.CYAN);
    private final Ui.Pastille pastilleTheme = new Ui.Pastille("", Ui.ROSE);
    private final Ui.Pastille pastilleNiveau = new Ui.Pastille("", Ui.VERT);
    private final Ui.Barre barre = new Ui.Barre();
    private final Ui.Carte carteQuestion = new Ui.Carte(new BorderLayout(0, 22));
    private final JLabel enonce = Ui.texte("", 26, Font.BOLD, Ui.TEXTE);
    private final Ui.Champ reponse = new Ui.Champ("Ta réponse…");
    private final Ui.Bouton valider = new Ui.Bouton("Valider", Ui.VIOLET, Ui.ROSE);
    private final Ui.Banniere retour = new Ui.Banniere();
    private final Timer chrono = new Timer(1000, e -> majChrono());

    // Résultat
    private final EcranResultat ecranResultat = new EcranResultat();
    private final Anneau anneau = new Anneau();
    private final JLabel resultatMessage = Ui.texte("", 24, Font.BOLD, Ui.TEXTE);
    private final JLabel resultatDetail = Ui.texte("", 15, Font.PLAIN, Ui.DISCRET);
    private final Ui.Bouton rejouer = new Ui.Bouton("Rejouer", Ui.VIOLET, Ui.ROSE);

    private List<Question> questions;
    private int indice;
    private int score;
    private long debut;
    private boolean enAttenteSuivante;

    public QuizFenetre() {
        super("Quiz Java", 940, 660, null);
        ecrans.setOpaque(false);
        ecrans.add(creerAccueil(), ACCUEIL);
        ecrans.add(creerEcranQuestion(), QUESTION);
        ecrans.add(creerEcranResultat(), RESULTAT);
        contenu(ecrans);
        afficher(ACCUEIL);
    }

    // ------------------------------------------------------------------ construction des écrans

    private JPanel creerAccueil() {
        choixTheme.auChangement(i -> majNombreQuestions());
        choixDifficulte.auChangement(i -> majNombreQuestions());
        majNombreQuestions();
        commencer.addActionListener(e -> commencer());

        Ui.Carte carte = new Ui.Carte(new BorderLayout());
        carte.add(Ui.colonne(
                Ui.texte("THÈME", 12, Font.BOLD, Ui.DISCRET),
                Ui.espace(10),
                choixTheme,
                Ui.espace(22),
                Ui.texte("DIFFICULTÉ", 12, Font.BOLD, Ui.DISCRET),
                Ui.espace(10),
                choixDifficulte,
                Ui.espace(26),
                Ui.ligne(16, commencer, nombreQuestions)));

        JPanel colonne = Ui.colonne(
                Ui.texte("ATELIER JAVA + IA", 13, Font.BOLD, Ui.CYAN),
                Ui.espace(4),
                new Ui.TitreDegrade("Quiz Java", 64),
                Ui.texte("Teste tes connaissances en Java et en informatique.", 17, Font.PLAIN, Ui.DISCRET),
                Ui.espace(30),
                carte,
                Ui.espace(18),
                Ui.texte("Astuce : la touche Entrée valide et passe à la question suivante.", 13, Font.PLAIN, Ui.DISCRET));
        colonne.setPreferredSize(new Dimension(560, colonne.getPreferredSize().height));
        return Ui.centre(colonne);
    }

    private JPanel creerEcranQuestion() {
        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        haut.add(progression, BorderLayout.WEST);
        haut.add(Ui.ligne(8, pastilleScore, pastilleChrono), BorderLayout.EAST);

        JPanel entete = new JPanel(new BorderLayout(0, 14));
        entete.setOpaque(false);
        entete.add(haut, BorderLayout.NORTH);
        entete.add(barre, BorderLayout.CENTER);

        valider.addActionListener(e -> {
            if (enAttenteSuivante) {
                questionSuivante();
            } else {
                verifier();
            }
        });

        JPanel saisie = new JPanel(new BorderLayout(12, 0));
        saisie.setOpaque(false);
        saisie.add(reponse, BorderLayout.CENTER);
        saisie.add(valider, BorderLayout.EAST);

        JPanel bas = new JPanel(new BorderLayout(0, 16));
        bas.setOpaque(false);
        bas.add(saisie, BorderLayout.NORTH);
        bas.add(retour, BorderLayout.SOUTH);

        carteQuestion.setBorder(new EmptyBorder(30, 34, 30, 34));
        carteQuestion.add(Ui.ligne(8, pastilleTheme, pastilleNiveau), BorderLayout.NORTH);
        carteQuestion.add(enonce, BorderLayout.CENTER);
        carteQuestion.add(bas, BorderLayout.SOUTH);

        // La carte est centrée verticalement et prend toute la largeur
        JPanel milieu = new JPanel(new GridBagLayout());
        milieu.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 30, 0);
        milieu.add(carteQuestion, c);

        JPanel ecran = new JPanel(new BorderLayout(0, 20));
        ecran.setOpaque(false);
        ecran.setBorder(new EmptyBorder(22, 60, 10, 60));
        ecran.add(entete, BorderLayout.NORTH);
        ecran.add(milieu, BorderLayout.CENTER);
        return ecran;
    }

    private JPanel creerEcranResultat() {
        rejouer.addActionListener(e -> afficher(ACCUEIL));
        Ui.Bouton quitter = Ui.Bouton.fantome("Quitter", 14);
        quitter.addActionListener(e -> fermer());

        anneau.setPreferredSize(new Dimension(230, 230));
        anneau.setMaximumSize(new Dimension(230, 230));
        JPanel colonne = Ui.colonne(
                Ui.texte("RÉSULTAT", 13, Font.BOLD, Ui.CYAN),
                Ui.espace(14),
                anneau,
                Ui.espace(22),
                resultatMessage,
                Ui.espace(6),
                resultatDetail,
                Ui.espace(28),
                Ui.ligne(12, rejouer, quitter));
        ecranResultat.add(colonne);
        return ecranResultat;
    }

    // ------------------------------------------------------------------ déroulement

    private void afficher(String ecran) {
        cartes.show(ecrans, ecran);
        switch (ecran) {
            case ACCUEIL -> getRootPane().setDefaultButton(commencer);
            case QUESTION -> getRootPane().setDefaultButton(valider);
            default -> getRootPane().setDefaultButton(rejouer);
        }
    }

    private void majNombreQuestions() {
        int n = Quiz.selectionner(THEMES[choixTheme.selection()], choixDifficulte.selection()).size();
        nombreQuestions.setText(n + " question" + (n > 1 ? "s" : ""));
        commencer.setEnabled(n > 0);
    }

    private void commencer() {
        questions = Quiz.selectionner(THEMES[choixTheme.selection()], choixDifficulte.selection());
        Collections.shuffle(questions);
        indice = 0;
        score = 0;
        debut = System.currentTimeMillis();
        pastilleScore.setText("Score 0");
        pastilleChrono.setText("0:00");
        barre.valeur(0);
        chrono.restart();
        afficherQuestion();
        afficher(QUESTION);
        // Le champ n'est visible qu'après le changement d'écran : on demande le focus juste après.
        SwingUtilities.invokeLater(reponse::requestFocusInWindow);
    }

    private void afficherQuestion() {
        Question q = questions.get(indice);
        progression.setText("Question " + (indice + 1) + " / " + questions.size());
        pastilleTheme.setText(q.getTheme());
        pastilleNiveau.setText(q.getDifficulte() == 1 ? "Facile" : "Difficile");
        pastilleNiveau.couleur(q.getDifficulte() == 1 ? Ui.VERT : Ui.AMBRE);
        // Le HTML permet le retour à la ligne dans un JLabel (Swing convertit les px en points : 470 px ≈ 625 pixels)
        enonce.setText("<html><body style='width:470px'>" + q.getEnonce() + "</body></html>");
        reponse.setText("");
        reponse.setEditable(true);
        retour.cacher();
        valider.setText("Valider");
        enAttenteSuivante = false;
        reponse.requestFocusInWindow();
    }

    private void verifier() {
        Question q = questions.get(indice);
        String saisie = reponse.getText().trim();

        if (saisie.isEmpty()) {
            retour.montrer("Pas de réponse. C'était « " + q.getReponse() + " ».", Ui.Banniere.Type.INFO, 0);
            carteQuestion.eclairer(Ui.AMBRE);
        } else if (q.estCorrecte(saisie)) {
            score++;
            retour.montrer("Bonne réponse !", Ui.Banniere.Type.SUCCES, 0);
            carteQuestion.eclairer(Ui.VERT);
            pastilleScore.setText("Score " + score);
        } else {
            retour.montrer("Raté. La bonne réponse était « " + q.getReponse() + " ».", Ui.Banniere.Type.ERREUR, 0);
            carteQuestion.eclairer(Ui.ROUGE);
        }

        reponse.setEditable(false);
        enAttenteSuivante = true;
        barre.valeur((indice + 1f) / questions.size());
        valider.setText(indice + 1 < questions.size() ? "Suivante  →" : "Voir le résultat");
    }

    private void questionSuivante() {
        indice++;
        if (indice < questions.size()) {
            afficherQuestion();
        } else {
            afficherResultat();
        }
    }

    private void majChrono() {
        long s = (System.currentTimeMillis() - debut) / 1000;
        pastilleChrono.setText(String.format("%d:%02d", s / 60, s % 60));
    }

    private void afficherResultat() {
        chrono.stop();
        long s = (System.currentTimeMillis() - debut) / 1000;
        int total = questions.size();
        float ratio = (float) score / total;

        resultatMessage.setText(Quiz.messagePalier(score, total));
        resultatDetail.setText(score + " bonne" + (score > 1 ? "s" : "") + " réponse" + (score > 1 ? "s" : "")
                + " sur " + total + "  ·  " + String.format("%d min %02d s", s / 60, s % 60));
        anneau.afficher(score, total);
        afficher(RESULTAT);
        if (ratio >= 0.75f) {
            ecranResultat.lancerConfettis();
        }
    }

    // ------------------------------------------------------------------ composants propres au quiz

    /** Anneau de score qui se remplit progressivement, couleur selon le résultat. */
    private static final class Anneau extends JComponent {
        private final Ui.Anim progression = new Ui.Anim(this, 0, 0.045f);
        private int score;
        private int total;

        void afficher(int score, int total) {
            this.score = score;
            this.total = total;
            progression.fixer(0);
            progression.vers(total == 0 ? 0 : (float) score / total);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Ui.lisse(g);
            int taille = Math.min(getWidth(), getHeight()) - 24;
            float x = (getWidth() - taille) / 2f;
            float y = (getHeight() - taille) / 2f;
            float v = progression.get();
            float cible = total == 0 ? 0 : (float) score / total;
            Color c1 = cible >= 0.75f ? Ui.VERT : cible >= 0.5f ? Ui.VIOLET : Ui.ROSE;
            Color c2 = cible >= 0.5f ? Ui.CYAN : Ui.AMBRE;

            g2.setColor(Ui.alpha(c1, 25));
            g2.fill(new Ellipse2D.Float(x - 8, y - 8, taille + 16, taille + 16));
            g2.setStroke(new BasicStroke(18, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Ui.alpha(Color.WHITE, 25));
            g2.draw(new Ellipse2D.Float(x, y, taille, taille));
            if (v > 0.001f) {
                g2.setPaint(new GradientPaint(x, y, c1, x + taille, y + taille, c2));
                g2.draw(new Arc2D.Float(x, y, taille, taille, 90, -360 * v, Arc2D.OPEN));
            }

            String pourcent = Math.round(v * 100) + "%";
            g2.setFont(Ui.police(Font.BOLD, 50));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Ui.TEXTE);
            float cy = getHeight() / 2f;
            g2.drawString(pourcent, (getWidth() - fm.stringWidth(pourcent)) / 2f, cy + fm.getAscent() / 2f - 12);
            String detail = score + " / " + total;
            g2.setFont(Ui.police(Font.BOLD, 15));
            fm = g2.getFontMetrics();
            g2.setColor(Ui.DISCRET);
            g2.drawString(detail, (getWidth() - fm.stringWidth(detail)) / 2f, cy + 34);
            g2.dispose();
        }
    }

    /** Écran de résultat capable de faire pleuvoir des confettis par-dessus son contenu. */
    private static final class EcranResultat extends JPanel {
        private static final Color[] COULEURS = {Ui.VIOLET, Ui.ROSE, Ui.CYAN, Ui.VERT, Ui.AMBRE, Color.WHITE};
        // chaque confetti : x, y, vitesse x, vitesse y, angle, vitesse de rotation, couleur, taille
        private final List<float[]> confettis = new ArrayList<>();
        private final Random hasard = new Random();
        private final Timer animation = new Timer(16, e -> avancer());

        EcranResultat() {
            super(new GridBagLayout());
            setOpaque(false);
        }

        void lancerConfettis() {
            confettis.clear();
            int w = Math.max(getWidth(), 600);
            for (int i = 0; i < 180; i++) {
                confettis.add(new float[] {
                    hasard.nextFloat() * w,
                    -hasard.nextFloat() * 400 - 10,
                    (hasard.nextFloat() - 0.5f) * 3f,
                    2f + hasard.nextFloat() * 3f,
                    hasard.nextFloat() * 6.28f,
                    (hasard.nextFloat() - 0.5f) * 0.3f,
                    hasard.nextInt(COULEURS.length),
                    6 + hasard.nextFloat() * 6
                });
            }
            animation.start();
        }

        private void avancer() {
            confettis.removeIf(c -> c[1] > getHeight() + 20);
            for (float[] c : confettis) {
                c[3] += 0.06f;   // gravité
                c[2] *= 0.995f;  // frottement de l'air
                c[0] += c[2] + (float) Math.sin(c[1] / 30) * 0.6f;
                c[1] += c[3];
                c[4] += c[5];
            }
            if (confettis.isEmpty()) {
                animation.stop();
            }
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g); // d'abord le contenu, puis les confettis par-dessus
            if (confettis.isEmpty()) {
                return;
            }
            Graphics2D g2 = Ui.lisse(g);
            for (float[] c : confettis) {
                Graphics2D g3 = (Graphics2D) g2.create();
                g3.translate(c[0], c[1]);
                g3.rotate(c[4]);
                g3.setColor(COULEURS[(int) c[6]]);
                g3.fill(new RoundRectangle2D.Float(-c[7] / 2, -c[7] / 4, c[7], c[7] / 2, 2, 2));
                g3.dispose();
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        Ui.demarrer(() -> new QuizFenetre().setVisible(true));
    }
}
