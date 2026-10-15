package chatbot;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Version fenêtrée du chatbot, façon messagerie moderne.
 * Elle ne connaît que l'interface AiClient : Mock ou HTTP, elle fonctionne pareil.
 */
public class ChatFenetre extends Ui.Fenetre {

    private static final String BIENVENUE = "Salut ! Je suis ton tuteur Java. Pose-moi une question : "
            + "je t'explique avec des exemples, sans te donner la solution toute faite.";
    private static final String[] SUGGESTIONS = {
        "C'est quoi une interface ?",
        "Explique try-with-resources",
        "ArrayList ou tableau ?"
    };

    private final AiClient client;
    private final boolean horsLigne;
    private final List<String> transcription = new ArrayList<>();

    private final Fil fil = new Fil();
    private final JScrollPane defilement = Ui.defilement(fil);
    private final Ui.Champ saisie = new Ui.Champ("Pose ta question sur Java…  (/help pour les commandes)");
    private final BoutonEnvoyer envoyer = new BoutonEnvoyer();
    private final JPanel suggestions = Ui.ligne(8);
    private Indicateur indicateur;

    public ChatFenetre(AiClient client) {
        super("Tuteur Java", 920, 760, null);
        this.client = client;
        this.horsLigne = client instanceof MockAiClient;

        JPanel contenu = new JPanel(new BorderLayout());
        contenu.setOpaque(false);
        contenu.add(creerEntete(), BorderLayout.NORTH);
        contenu.add(defilement, BorderLayout.CENTER);
        contenu.add(creerZoneSaisie(), BorderLayout.SOUTH);
        contenu(contenu);

        envoyer.addActionListener(e -> envoyer(saisie.getText()));
        saisie.addActionListener(e -> envoyer(saisie.getText()));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                saisie.requestFocusInWindow();
            }
        });

        demarrerConversation();
    }

    // ------------------------------------------------------------------ construction

    private JPanel creerEntete() {
        JComponent avatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Ui.lisse(g);
                g2.setColor(Ui.alpha(Ui.VIOLET, 60));
                g2.fill(new Ellipse2D.Float(0, 0, 52, 52));
                g2.setPaint(new GradientPaint(0, 0, Ui.VIOLET, 52, 52, Ui.ROSE));
                g2.fill(new Ellipse2D.Float(4, 4, 44, 44));
                g2.setFont(Ui.police(Font.BOLD, 17));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString("IA", 26 - fm.stringWidth("IA") / 2f, 26 - fm.getHeight() / 2f + fm.getAscent());
                // pastille d'état en bas à droite
                g2.setColor(Ui.FOND_1);
                g2.fill(new Ellipse2D.Float(36, 36, 16, 16));
                g2.setColor(horsLigne ? Ui.AMBRE : Ui.VERT);
                g2.fill(new Ellipse2D.Float(39, 39, 10, 10));
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(52, 52));

        String etat = horsLigne
                ? "Hors-ligne · réponses factices (pas de clé AI_API_KEY)"
                : "En ligne · Groq llama-3.1-8b-instant";
        JPanel identite = Ui.colonne(
                Ui.texte("Tuteur Java", 19, Font.BOLD, Ui.TEXTE),
                Ui.espace(2),
                Ui.texte(etat, 13, Font.PLAIN, horsLigne ? Ui.AMBRE : Ui.VERT));

        JPanel gauche = new JPanel(new BorderLayout(14, 0));
        gauche.setOpaque(false);
        gauche.add(avatar, BorderLayout.WEST);
        gauche.add(Ui.centreVertical(identite), BorderLayout.CENTER);

        Ui.Bouton nouvelle = Ui.Bouton.fantome("Nouvelle conversation", 13);
        nouvelle.addActionListener(e -> reinitialiser());
        Ui.Bouton enregistrer = Ui.Bouton.fantome("Enregistrer", 13);
        enregistrer.addActionListener(e -> enregistrer());

        JPanel entete = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Ui.lisse(g);
                g2.setColor(Ui.alpha(Color.WHITE, 22));
                g2.fillRect(28, getHeight() - 1, getWidth() - 56, 1);
                g2.dispose();
            }
        };
        entete.setOpaque(false);
        entete.setBorder(new EmptyBorder(4, 28, 16, 28));
        entete.add(gauche, BorderLayout.WEST);
        entete.add(Ui.centreVertical(Ui.ligne(8, nouvelle, enregistrer)), BorderLayout.EAST);
        return entete;
    }

    private JPanel creerZoneSaisie() {
        for (String s : SUGGESTIONS) {
            Ui.Bouton b = Ui.Bouton.fantome(s, 13);
            b.addActionListener(e -> envoyer(s));
            suggestions.add(b);
        }

        JPanel ligneSaisie = new JPanel(new BorderLayout(12, 0));
        ligneSaisie.setOpaque(false);
        ligneSaisie.add(saisie, BorderLayout.CENTER);
        ligneSaisie.add(envoyer, BorderLayout.EAST);

        JPanel zone = new JPanel(new BorderLayout(0, 12));
        zone.setOpaque(false);
        zone.setBorder(new EmptyBorder(10, 28, 22, 28));
        zone.add(suggestions, BorderLayout.NORTH);
        zone.add(ligneSaisie, BorderLayout.CENTER);
        return zone;
    }

    // ------------------------------------------------------------------ conversation

    private void demarrerConversation() {
        fil.removeAll();
        ajouter(new Bulle(BIENVENUE, Genre.IA));
        if (horsLigne) {
            ajouter(new Bulle("Mode hors-ligne : définis AI_API_KEY (dans projet3/.env pour Cursor) "
                    + "pour parler à la vraie IA.", Genre.INFO));
        }
        suggestions.setVisible(true);
    }

    private void envoyer(String texte) {
        String question = texte.trim();
        if (question.isEmpty() || !envoyer.isEnabled()) {
            return;
        }
        saisie.setText("");
        if (question.startsWith("/")) {
            commande(question.toLowerCase());
            return;
        }

        suggestions.setVisible(false);
        ajouter(new Bulle(question, Genre.MOI));
        transcription.add("Toi : " + question);
        occupe(true);

        // L'appel réseau peut prendre plusieurs secondes : on le fait hors du thread Swing,
        // sinon la fenêtre se fige. done() est ensuite rappelé sur le thread Swing.
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                if (horsLigne) {
                    Thread.sleep(1200); // le faux client répond instantanément : on simule un délai pour l'animation
                }
                return client.ask(question);
            }

            @Override
            protected void done() {
                occupe(false);
                try {
                    String reponse = get();
                    ajouter(new Bulle(reponse, Genre.IA));
                    transcription.add("IA : " + reponse);
                } catch (ExecutionException e) {
                    String message = e.getCause() instanceof AiException
                            ? e.getCause().getMessage()
                            : "Erreur inattendue : " + e.getCause();
                    ajouter(new Bulle(message, Genre.ERREUR));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void commande(String commande) {
        switch (commande) {
            case "/help" -> ajouter(new Bulle("/help : cette aide\n/reset : nouvelle conversation\n"
                    + "/save : enregistre la conversation dans un fichier\n/quit : quitte", Genre.INFO));
            case "/reset" -> reinitialiser();
            case "/save" -> enregistrer();
            case "/quit" -> fermer();
            default -> ajouter(new Bulle("Commande inconnue : " + commande + ". Tape /help.", Genre.ERREUR));
        }
    }

    private void occupe(boolean enCours) {
        envoyer.setEnabled(!enCours);
        if (enCours) {
            indicateur = new Indicateur();
            ajouter(indicateur);
        } else if (indicateur != null) {
            fil.remove(indicateur);
            indicateur = null;
            fil.revalidate();
            saisie.requestFocusInWindow();
        }
    }

    private void ajouter(JComponent element) {
        fil.add(element);
        fil.revalidate();
        fil.repaint();
        // Une fois la mise en page refaite, on descend tout en bas
        SwingUtilities.invokeLater(() -> {
            defilement.validate();
            JScrollBar barre = defilement.getVerticalScrollBar();
            barre.setValue(barre.getMaximum());
        });
    }

    private void reinitialiser() {
        client.reset();
        transcription.clear();
        demarrerConversation();
        fil.revalidate();
        fil.repaint();
    }

    private void enregistrer() {
        String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path fichier = Path.of("conversation-" + horodatage + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(fichier, StandardCharsets.UTF_8)) {
            for (String ligne : transcription) {
                writer.write(ligne);
                writer.newLine();
            }
            ajouter(new Bulle("Conversation enregistrée dans " + fichier.toAbsolutePath(), Genre.INFO));
        } catch (IOException e) {
            ajouter(new Bulle("Sauvegarde impossible : " + e.getMessage(), Genre.ERREUR));
        }
    }

    // ------------------------------------------------------------------ composants propres au chat

    private enum Genre { MOI, IA, ERREUR, INFO }

    /** Une bulle de message : texte sélectionnable, coins arrondis, apparition en fondu glissé. */
    private static final class Bulle extends JPanel {
        private static final int PAD_X = 18;
        private static final int PAD_Y = 12;
        private final Genre genre;
        private final JTextArea zone = new JTextArea();
        private final Ui.Anim apparition = new Ui.Anim(this, 0, 0.14f);

        Bulle(String texte, Genre genre) {
            super(null);
            this.genre = genre;
            setOpaque(false);
            zone.setText(texte);
            zone.setLineWrap(true);
            zone.setWrapStyleWord(true);
            zone.setEditable(false);
            zone.setOpaque(false);
            zone.setBorder(null);
            zone.setFont(Ui.police(Font.PLAIN, genre == Genre.INFO ? 13.5f : 15.5f));
            zone.setForeground(switch (genre) {
                case MOI -> Color.WHITE;
                case IA -> Ui.TEXTE;
                case ERREUR -> Ui.melange(Ui.ROUGE, Color.WHITE, 0.55f);
                case INFO -> Ui.melange(Ui.AMBRE, Color.WHITE, 0.45f);
            });
            zone.setSelectionColor(Ui.alpha(Color.WHITE, 70));
            zone.setSelectedTextColor(Color.WHITE);
            add(zone);
            apparition.vers(1);
        }

        /** Taille de la bulle pour une largeur maximale donnée (le texte passe à la ligne si besoin). */
        Dimension taille(int largeurMax) {
            FontMetrics fm = zone.getFontMetrics(zone.getFont());
            int naturelle = 0;
            for (String ligne : zone.getText().split("\n")) {
                naturelle = Math.max(naturelle, fm.stringWidth(ligne));
            }
            int largeurTexte = Math.max(24, Math.min(naturelle + 6, largeurMax - 2 * PAD_X));
            zone.setSize(largeurTexte, Short.MAX_VALUE);
            int hauteur = zone.getPreferredSize().height;
            zone.setBounds(PAD_X, PAD_Y, largeurTexte, hauteur);
            return new Dimension(largeurTexte + 2 * PAD_X, hauteur + 2 * PAD_Y);
        }

        @Override
        public void paint(Graphics g) {
            // Le fondu s'applique à toute la bulle, texte compris
            Graphics2D g2 = (Graphics2D) g.create();
            float a = apparition.get();
            Ui.opacite(g2, a);
            g2.translate(0, (1 - a) * 14);
            super.paint(g2);
            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Ui.lisse(g);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D forme = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 22, 22);
            switch (genre) {
                case MOI -> {
                    g2.setPaint(new GradientPaint(0, 0, Ui.VIOLET, w, h, Ui.ROSE));
                    g2.fill(forme);
                    g2.setPaint(new GradientPaint(0, 0, Ui.alpha(Color.WHITE, 45), 0, h * 0.7f, Ui.alpha(Color.WHITE, 0)));
                    g2.fill(forme);
                }
                case IA -> {
                    g2.setColor(Ui.alpha(Color.WHITE, 20));
                    g2.fill(forme);
                    g2.setColor(Ui.alpha(Color.WHITE, 36));
                    g2.draw(forme);
                }
                case ERREUR -> {
                    g2.setColor(Ui.alpha(Ui.ROUGE, 40));
                    g2.fill(forme);
                    g2.setColor(Ui.alpha(Ui.ROUGE, 150));
                    g2.draw(forme);
                }
                case INFO -> {
                    g2.setColor(Ui.alpha(Ui.AMBRE, 26));
                    g2.fill(forme);
                    g2.setColor(Ui.alpha(Ui.AMBRE, 110));
                    g2.draw(forme);
                }
            }
            g2.dispose();
        }
    }

    /** Trois points qui rebondissent pendant que l'IA "écrit". */
    private static final class Indicateur extends JComponent {
        private final Timer animation;
        private float phase = 0;

        Indicateur() {
            setPreferredSize(new Dimension(78, 44));
            animation = new Timer(16, e -> {
                phase += 0.12f;
                repaint();
            });
        }

        @Override
        public void addNotify() {
            super.addNotify();
            animation.start();
        }

        @Override
        public void removeNotify() {
            animation.stop();
            super.removeNotify();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Ui.lisse(g);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D forme = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 22, 22);
            g2.setColor(Ui.alpha(Color.WHITE, 20));
            g2.fill(forme);
            g2.setColor(Ui.alpha(Color.WHITE, 36));
            g2.draw(forme);
            for (int i = 0; i < 3; i++) {
                float rebond = (float) Math.max(0, Math.sin(phase - i * 0.7f));
                float x = 22 + i * 17;
                float y = h / 2f - rebond * 6;
                g2.setColor(Ui.melange(Ui.DISCRET, Ui.CYAN, rebond));
                g2.fill(new Ellipse2D.Float(x - 4, y - 4, 8, 8));
            }
            g2.dispose();
        }
    }

    /** Fil de discussion : mes bulles à droite, celles de l'IA à gauche, les infos au centre. */
    private static final class Fil extends JPanel implements Scrollable {
        Fil() {
            setOpaque(false);
            setLayout(new FilLayout());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return 200;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // la largeur suit la fenêtre, seule la hauteur défile
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static final class FilLayout implements LayoutManager {
        private static final int MARGE = 28;
        private static final int ECART = 12;

        @Override
        public void layoutContainer(Container parent) {
            disposer(parent, true);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(parent.getWidth(), disposer(parent, false));
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        /** Calcule (et applique si demandé) la position de chaque élément ; renvoie la hauteur totale. */
        private int disposer(Container parent, boolean appliquer) {
            int largeur = parent.getWidth() > 0 ? parent.getWidth() : 860;
            int largeurMax = (int) Math.min((largeur - 2 * MARGE) * 0.78, 660);
            int y = 18;
            for (Component c : parent.getComponents()) {
                Dimension d;
                int x;
                if (c instanceof Bulle b) {
                    d = b.taille(b.genre == Genre.INFO ? largeur - 2 * MARGE : largeurMax);
                    x = switch (b.genre) {
                        case MOI -> largeur - MARGE - d.width;
                        case INFO -> (largeur - d.width) / 2;
                        default -> MARGE;
                    };
                } else {
                    d = c.getPreferredSize();
                    x = MARGE;
                }
                if (appliquer) {
                    c.setBounds(x, y, d.width, d.height);
                }
                y += d.height + ECART;
            }
            return y + 8;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }
    }

    /** Bouton rond d'envoi avec une flèche dessinée. */
    private static final class BoutonEnvoyer extends JButton {
        private final Ui.Anim survol = new Ui.Anim(this, 0, 0.2f);

        BoutonEnvoyer() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(50, 50));
            setToolTipText("Envoyer (Entrée)");
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    survol.vers(1);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    survol.vers(0);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Ui.lisse(g);
            float s = isEnabled() ? survol.get() : 0;
            if (!isEnabled()) {
                Ui.opacite(g2, 0.35f);
            }
            int t = Math.min(getWidth(), getHeight());
            float x = (getWidth() - t) / 2f;
            float y = (getHeight() - t) / 2f;
            g2.setPaint(new GradientPaint(x, y, Ui.melange(Ui.VIOLET, Color.WHITE, s * 0.2f),
                    x + t, y + t, Ui.melange(Ui.ROSE, Color.WHITE, s * 0.2f)));
            g2.fill(new Ellipse2D.Float(x, y, t, t));
            // flèche "avion en papier"
            float cx = x + t / 2f + 1;
            float cy = y + t / 2f;
            Path2D.Float fleche = new Path2D.Float();
            fleche.moveTo(cx - 9, cy - 8);
            fleche.lineTo(cx + 10, cy);
            fleche.lineTo(cx - 9, cy + 8);
            fleche.lineTo(cx - 5, cy);
            fleche.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(fleche);
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(fleche);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        String cle = System.getenv("AI_API_KEY");
        AiClient client = (cle == null || cle.isBlank())
                ? new MockAiClient()
                : new HttpAiClient(cle.trim(), Main.MESSAGE_SYSTEME);
        Ui.demarrer(() -> new ChatFenetre(client).setVisible(true));
    }
}
