package chatbot;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntConsumer;

/**
 * Boîte à outils graphique maison (Swing + Java2D), style "arcade" sombre :
 * fond quasi noir avec des formes qui flottent, couleurs vives, boutons en relief, ombres dures.
 * Aucune bibliothèque externe : chaque composant se dessine lui-même dans paintComponent().
 * Ce fichier est identique dans les trois projets (seul le package change).
 */
final class Ui {

    private Ui() {
    }

    // ------------------------------------------------------------------ palette

    static final Color FOND_1 = new Color(0x0D0D14);       // fond de la fenêtre
    static final Color CARTE = new Color(0x1A1A28);        // fond des cartes
    static final Color CARTE_CLAIRE = new Color(0x242436); // survol
    static final Color BORD = new Color(0x34344E);
    static final Color OMBRE = new Color(0x050508);
    static final Color TEXTE = new Color(0xF6F6FB);
    static final Color DISCRET = new Color(0x9C9CB8);
    static final Color ENCRE = new Color(0x14141C);        // texte posé sur une couleur vive

    static final Color JAUNE = new Color(0xFFD23F);
    static final Color CORAIL = new Color(0xFF5D73);
    static final Color CYAN = new Color(0x2DE2D2);
    static final Color CITRON = new Color(0xA6F04C);
    static final Color BLEU = new Color(0x5B8CFF);
    static final Color ORANGE = new Color(0xFF9F43);

    // couleurs "de sens"
    static final Color VERT = CITRON;   // réussite
    static final Color ROUGE = CORAIL;  // erreur
    static final Color AMBRE = ORANGE;  // information

    static final Color[] ARC_EN_CIEL = {JAUNE, CORAIL, CYAN, CITRON, BLEU, ORANGE};

    static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    /** Mélange de deux couleurs : t = 0 donne a, t = 1 donne b. */
    static Color melange(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    static Color fonce(Color c) {
        return melange(c, Color.BLACK, 0.45f);
    }

    /** Texte foncé sur couleur claire, blanc sur couleur foncée. */
    static Color texteSur(Color fond) {
        double luminance = 0.299 * fond.getRed() + 0.587 * fond.getGreen() + 0.114 * fond.getBlue();
        return luminance > 150 ? ENCRE : Color.WHITE;
    }

    // ------------------------------------------------------------------ polices et dessin

    private static final String POLICE = choisirPolice("Segoe UI", "Inter", "Helvetica Neue", "Arial");
    private static final String POLICE_TITRE = choisirPolice("Segoe UI Black", "Arial Black", POLICE);

    private static String choisirPolice(String... voulues) {
        String[] disponibles = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String voulue : voulues) {
            for (String f : disponibles) {
                if (f.equals(voulue)) {
                    return f;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    static Font police(int style, float taille) {
        return new Font(POLICE, style, 12).deriveFont(taille);
    }

    static Font policeTitre(float taille) {
        return new Font(POLICE_TITRE, Font.BOLD, 12).deriveFont(taille);
    }

    /** Copie du Graphics avec l'anticrénelage activé (bords et textes lisses). */
    static Graphics2D lisse(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g2;
    }

    static void opacite(Graphics2D g2, float a) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, Math.min(1, a))));
    }

    /** Lance l'interface sur le thread Swing (EDT), avec quelques réglages globaux. */
    static void demarrer(Runnable creation) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            UIManager.put("ToolTip.background", CARTE);
            UIManager.put("ToolTip.foreground", TEXTE);
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(JAUNE, 2));
            UIManager.put("ToolTip.font", police(Font.PLAIN, 13));
            creation.run();
        });
    }

    static JLabel texte(String contenu, float taille, int style, Color couleur) {
        JLabel label = new JLabel(contenu);
        label.setFont(police(style, taille));
        label.setForeground(couleur);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Colonne verticale transparente, tous les éléments alignés à gauche. */
    static JPanel colonne(Component... elements) {
        JPanel panneau = new JPanel();
        panneau.setLayout(new BoxLayout(panneau, BoxLayout.Y_AXIS));
        panneau.setOpaque(false);
        panneau.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Component c : elements) {
            if (c instanceof JComponent jc) {
                jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            panneau.add(c);
        }
        return panneau;
    }

    /** Ligne horizontale transparente qui ne s'étire pas en hauteur dans une colonne. */
    static JPanel ligne(int ecart, Component... elements) {
        JPanel panneau = new JPanel(new FlowLayout(FlowLayout.LEFT, ecart, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panneau.setOpaque(false);
        panneau.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Component c : elements) {
            panneau.add(c);
        }
        return panneau;
    }

    static Component espace(int hauteur) {
        JComponent espace = (JComponent) Box.createRigidArea(new Dimension(0, hauteur));
        espace.setAlignmentX(Component.LEFT_ALIGNMENT);
        return espace;
    }

    /** Centre un composant dans la place disponible. */
    static JPanel centre(JComponent contenu) {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setOpaque(false);
        panneau.add(contenu);
        return panneau;
    }

    /** Centre un composant verticalement, collé à gauche. */
    static JPanel centreVertical(JComponent contenu) {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        panneau.add(contenu, c);
        return panneau;
    }

    /** Zone défilante transparente avec une barre de défilement fine et arrondie. */
    static JScrollPane defilement(JComponent vue) {
        JScrollPane sp = new JScrollPane(vue);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE); // évite les traces sur fond transparent
        sp.setBorder(null);
        sp.setViewportBorder(null);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollBar barre = sp.getVerticalScrollBar();
        barre.setOpaque(false);
        barre.setUnitIncrement(18);
        barre.setPreferredSize(new Dimension(12, 0));
        barre.setUI(new BasicScrollBarUI() {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return boutonVide();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return boutonVide();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                // piste invisible
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) {
                    return;
                }
                Graphics2D g2 = lisse(g);
                g2.setColor(isThumbRollover() ? JAUNE : BORD);
                g2.fill(new RoundRectangle2D.Float(r.x + 3, r.y + 2, r.width - 6, r.height - 4, 6, 6));
                g2.dispose();
            }

            private JButton boutonVide() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        return sp;
    }

    // ------------------------------------------------------------------ petits dessins

    static void coche(Graphics2D g2, float cx, float cy, float taille, Color couleur) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(cx - taille * 0.45f, cy + taille * 0.02f);
        p.lineTo(cx - taille * 0.12f, cy + taille * 0.32f);
        p.lineTo(cx + taille * 0.45f, cy - taille * 0.3f);
        g2.setColor(couleur);
        g2.setStroke(new BasicStroke(taille * 0.18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(p);
    }

    static void croix(Graphics2D g2, float cx, float cy, float taille, Color couleur) {
        float d = taille * 0.3f;
        g2.setColor(couleur);
        g2.setStroke(new BasicStroke(taille * 0.15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Float(cx - d, cy - d, cx + d, cy + d));
        g2.draw(new Line2D.Float(cx + d, cy - d, cx - d, cy + d));
    }

    /** Dessine une pastille de texte dont le bord DROIT est en xDroite ; renvoie son bord gauche. */
    static int pastille(Graphics2D g2, String texte, Color couleur, int xDroite, int yCentre) {
        g2.setFont(police(Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int largeur = fm.stringWidth(texte) + 22;
        int hauteur = 26;
        int x = xDroite - largeur;
        int y = yCentre - hauteur / 2;
        RoundRectangle2D forme = new RoundRectangle2D.Float(x, y, largeur, hauteur, 10, 10);
        g2.setColor(alpha(couleur, 34));
        g2.fill(forme);
        g2.setColor(couleur);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(forme);
        g2.drawString(texte, x + 11, y + (hauteur - fm.getHeight()) / 2 + fm.getAscent());
        return x;
    }

    /** Coupe un texte trop long avec "…". */
    static String tronquer(String texte, FontMetrics fm, int largeurMax) {
        if (fm.stringWidth(texte) <= largeurMax) {
            return texte;
        }
        String s = texte;
        while (!s.isEmpty() && fm.stringWidth(s + "…") > largeurMax) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "…";
    }

    static BufferedImage icone() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = lisse(img.getGraphics());
        g2.setColor(OMBRE);
        g2.fill(new RoundRectangle2D.Float(8, 8, 54, 54, 16, 16));
        g2.setColor(JAUNE);
        g2.fill(new RoundRectangle2D.Float(2, 2, 54, 54, 16, 16));
        g2.setColor(ENCRE);
        g2.fill(new Ellipse2D.Float(16, 20, 9, 9));
        g2.fill(new Ellipse2D.Float(33, 20, 9, 9));
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new java.awt.geom.Arc2D.Float(17, 24, 24, 18, 200, 140, java.awt.geom.Arc2D.OPEN));
        g2.dispose();
        return img;
    }

    // ------------------------------------------------------------------ animation

    /** Fait tendre une valeur vers une cible image par image (~60 i/s) et redessine le composant. */
    static final class Anim {
        private final JComponent composant;
        private final float vitesse;
        private final Timer timer;
        private float valeur;
        private float cible;

        Anim(JComponent composant, float depart, float vitesse) {
            this.composant = composant;
            this.valeur = depart;
            this.cible = depart;
            this.vitesse = vitesse;
            this.timer = new Timer(16, e -> pas());
        }

        void vers(float nouvelleCible) {
            cible = nouvelleCible;
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        void fixer(float v) {
            valeur = v;
            cible = v;
            composant.repaint();
        }

        float get() {
            return valeur;
        }

        private void pas() {
            valeur += (cible - valeur) * vitesse;
            if (Math.abs(cible - valeur) < 0.002f) {
                valeur = cible;
                timer.stop();
            }
            composant.repaint();
        }
    }

    // ------------------------------------------------------------------ fenêtre

    /** Fond quasi noir, grille de points, et des petites formes colorées qui flottent en tournant. */
    static class Fond extends JPanel {
        private final List<float[]> formes = new ArrayList<>(); // x, y, vx, vy, angle, vitesse angle, taille, type, couleur
        private final Random hasard = new Random(7);

        Fond(LayoutManager layout) {
            super(layout);
            setOpaque(true);
            new Timer(33, e -> avancer()).start();
        }

        private void avancer() {
            int w = getWidth();
            int h = getHeight();
            if (w == 0) {
                return;
            }
            if (formes.isEmpty()) {
                for (int i = 0; i < 16; i++) {
                    formes.add(new float[] {
                        hasard.nextFloat() * w, hasard.nextFloat() * h,
                        (hasard.nextFloat() - 0.5f) * 0.5f, (hasard.nextFloat() - 0.5f) * 0.5f,
                        hasard.nextFloat() * 6.28f, (hasard.nextFloat() - 0.5f) * 0.02f,
                        14 + hasard.nextFloat() * 22, hasard.nextInt(5), hasard.nextInt(ARC_EN_CIEL.length)
                    });
                }
            }
            for (float[] f : formes) {
                f[0] = (f[0] + f[2] + w + 60) % (w + 60);
                f[1] = (f[1] + f[3] + h + 60) % (h + 60);
                f[4] += f[5];
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(FOND_1);
            g2.fillRect(0, 0, w, h);

            // grille de points
            g2.setColor(new Color(255, 255, 255, 16));
            for (int x = 14; x < w; x += 28) {
                for (int y = 14; y < h; y += 28) {
                    g2.fillRect(x, y, 2, 2);
                }
            }

            // formes flottantes
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (float[] f : formes) {
                Graphics2D g3 = (Graphics2D) g2.create();
                g3.translate(f[0] - 30, f[1] - 30);
                g3.rotate(f[4]);
                g3.setColor(alpha(ARC_EN_CIEL[(int) f[8]], 70));
                g3.draw(forme((int) f[7], f[6]));
                g3.dispose();
            }

            // vignette : les bords sont plus sombres
            float r = Math.max(w, h) * 0.75f;
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(w / 2f, h / 2f), r, new float[] {0.4f, 1f},
                    new Color[] {new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)}));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }

        private static Shape forme(int type, float t) {
            float d = t / 2;
            switch (type) {
                case 0:
                    return new Ellipse2D.Float(-d, -d, t, t);
                case 1: {
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(0, -d);
                    p.lineTo(d, d * 0.8f);
                    p.lineTo(-d, d * 0.8f);
                    p.closePath();
                    return p;
                }
                case 2: {
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(-d, 0);
                    p.lineTo(d, 0);
                    p.moveTo(0, -d);
                    p.lineTo(0, d);
                    return p;
                }
                case 3:
                    return new Rectangle2D.Float(-d * 0.8f, -d * 0.8f, t * 0.8f, t * 0.8f);
                default: {
                    Path2D.Float p = new Path2D.Float(); // zigzag
                    p.moveTo(-d, 0);
                    for (int i = 1; i <= 4; i++) {
                        p.lineTo(-d + i * t / 4, (i % 2 == 0) ? 0 : -d * 0.5f);
                    }
                    return p;
                }
            }
        }
    }

    /** Fenêtre sans bordure système, avec sa propre barre de titre (déplaçable). */
    static class Fenetre extends JFrame {
        private final Fond fond = new Fond(new BorderLayout());
        private final Runnable avantFermeture;
        private Point prise;

        Fenetre(String titre, int largeur, int hauteur, Runnable avantFermeture) {
            super(titre);
            this.avantFermeture = avantFermeture;
            setUndecorated(true);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    fermer();
                }
            });
            fond.setBorder(BorderFactory.createLineBorder(BORD, 2));
            fond.add(barreDeTitre(titre), BorderLayout.NORTH);
            setContentPane(fond);
            setIconImage(icone());
            setSize(largeur, hauteur);
            setLocationRelativeTo(null);
        }

        void contenu(JComponent c) {
            fond.add(c, BorderLayout.CENTER);
        }

        void fermer() {
            if (avantFermeture != null) {
                avantFermeture.run();
            }
            dispose();
            System.exit(0);
        }

        private JPanel barreDeTitre(String titre) {
            JPanel barre = new JPanel(new BorderLayout());
            barre.setOpaque(false);
            barre.setBorder(new EmptyBorder(0, 16, 0, 0));
            barre.setPreferredSize(new Dimension(10, 42));

            JComponent logo = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = lisse(g);
                    float y = (getHeight() - 10) / 2f;
                    Color[] c = {CORAIL, JAUNE, CITRON};
                    for (int i = 0; i < 3; i++) {
                        g2.setColor(c[i]);
                        g2.fill(new Ellipse2D.Float(i * 15, y, 10, 10));
                    }
                    g2.dispose();
                }
            };
            logo.setPreferredSize(new Dimension(56, 42));
            JPanel gauche = new JPanel(new BorderLayout());
            gauche.setOpaque(false);
            gauche.add(logo, BorderLayout.WEST);
            gauche.add(texte(titre, 13, Font.BOLD, DISCRET), BorderLayout.CENTER);

            JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            droite.setOpaque(false);
            droite.add(new BoutonFenetre(false, () -> setState(ICONIFIED)));
            droite.add(new BoutonFenetre(true, this::fermer));

            barre.add(gauche, BorderLayout.WEST);
            barre.add(droite, BorderLayout.EAST);

            MouseAdapter deplacement = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    prise = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    Point p = e.getLocationOnScreen();
                    setLocation(p.x - prise.x, p.y - prise.y);
                }
            };
            barre.addMouseListener(deplacement);
            barre.addMouseMotionListener(deplacement);
            return barre;
        }
    }

    /** Bouton réduire / fermer de la barre de titre. */
    private static final class BoutonFenetre extends JComponent {
        private final boolean fermeture;
        private final Anim survol = new Anim(this, 0, 0.25f);

        BoutonFenetre(boolean fermeture, Runnable action) {
            this.fermeture = fermeture;
            setPreferredSize(new Dimension(46, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    survol.vers(1);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    survol.vers(0);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            float s = survol.get();
            g2.setColor(fermeture ? alpha(CORAIL, (int) (255 * s)) : alpha(Color.WHITE, (int) (25 * s)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            if (fermeture) {
                croix(g2, cx, cy, 22, melange(TEXTE, ENCRE, s));
            } else {
                g2.setColor(TEXTE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
                g2.draw(new Line2D.Float(cx - 5, cy, cx + 5, cy));
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ composants

    /** Carte sombre avec une ombre dure décalée ; son bord peut s'allumer d'une couleur. */
    static class Carte extends JPanel {
        static final int DECALAGE = 6;
        private final Anim eclat = new Anim(this, 0, 0.04f);
        private Color couleurEclat = JAUNE;

        Carte(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(24, 26, 24, 26));
        }

        /** L'ombre occupe DECALAGE pixels à droite et en bas : on les ajoute aux marges. */
        @Override
        public Insets getInsets() {
            Insets i = super.getInsets();
            return new Insets(i.top, i.left, i.bottom + DECALAGE, i.right + DECALAGE);
        }

        /** Fait briller le bord de la carte puis s'éteindre doucement. */
        void eclairer(Color couleur) {
            couleurEclat = couleur;
            eclat.fixer(1);
            eclat.vers(0);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            float e = eclat.get();
            int w = getWidth() - DECALAGE;
            int h = getHeight() - DECALAGE;
            g2.setColor(e > 0.01f ? melange(OMBRE, fonce(couleurEclat), e) : OMBRE);
            g2.fill(new RoundRectangle2D.Float(DECALAGE, DECALAGE, w, h, 22, 22));
            RoundRectangle2D forme = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 22, 22);
            g2.setColor(melange(CARTE, couleurEclat, e * 0.12f));
            g2.fill(forme);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(melange(BORD, couleurEclat, e));
            g2.draw(forme);
            g2.dispose();
        }
    }

    /** Dessine une forme "en relief" : une base foncée dessous, la face au-dessus, enfoncée de 'enfoncement'. */
    private static RoundRectangle2D relief(Graphics2D g2, int w, int h, int profondeur, float enfoncement,
                                           Color face, Color base, Color bord, float arrondi) {
        g2.setColor(base);
        g2.fill(new RoundRectangle2D.Float(0, profondeur, w, h - profondeur, arrondi, arrondi));
        RoundRectangle2D dessus = new RoundRectangle2D.Float(0, enfoncement, w, h - profondeur, arrondi, arrondi);
        g2.setColor(face);
        g2.fill(dessus);
        if (bord != null) {
            g2.setColor(bord);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(1, enfoncement + 1, w - 2, h - profondeur - 2, arrondi, arrondi));
        }
        return dessus;
    }

    /** Bouton en relief façon jeu vidéo : il se soulève au survol et s'enfonce au clic. */
    static class Bouton extends JButton {
        private static final int PROFONDEUR = 6;
        private final Color couleur;
        private final Anim survol = new Anim(this, 0, 0.25f);

        /** Bouton principal, de la couleur donnée. */
        Bouton(String texte, Color couleur) {
            this(texte, couleur, 15);
        }

        /** Bouton secondaire, sombre. */
        static Bouton fantome(String texte, float taille) {
            return new Bouton(texte, null, taille);
        }

        private Bouton(String texte, Color couleur, float taille) {
            super(texte);
            this.couleur = couleur;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(police(Font.BOLD, taille));
            setForeground(TEXTE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int v = Math.round(taille * 0.7f);
            setBorder(new EmptyBorder(v, Math.round(taille * 1.4f), v + PROFONDEUR, Math.round(taille * 1.4f)));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    survol.vers(1);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    survol.vers(0);
                }
            });
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            int w = getWidth();
            int h = getHeight();
            float s = isEnabled() ? survol.get() : 0;
            if (!isEnabled()) {
                opacite(g2, 0.35f);
            }
            boolean presse = getModel().isPressed() && getModel().isArmed();
            float enfoncement = presse ? PROFONDEUR - 1 : 2 - 2 * s;
            Color face;
            Color base;
            Color bord;
            Color texte;
            if (couleur == null) {
                face = melange(CARTE, CARTE_CLAIRE, s);
                base = OMBRE;
                bord = melange(BORD, JAUNE, s);
                texte = TEXTE;
            } else {
                face = melange(couleur, Color.WHITE, s * 0.15f);
                base = fonce(couleur);
                bord = null;
                texte = texteSur(couleur);
            }
            relief(g2, w, h, PROFONDEUR, enfoncement, face, base, bord, 16);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(texte);
            String t = getText();
            float hauteurFace = h - PROFONDEUR;
            g2.drawString(t, (w - fm.stringWidth(t)) / 2f, enfoncement + (hauteurFace - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }

    /** Champ de saisie sombre ; son bord s'allume en cyan quand il a le focus. */
    static class Champ extends JTextField {
        private final String exemple;
        private final Anim focus = new Anim(this, 0, 0.2f);

        Champ(String exemple) {
            this.exemple = exemple;
            setOpaque(false);
            setBorder(new EmptyBorder(13, 16, 13, 16));
            setForeground(TEXTE);
            setCaretColor(JAUNE);
            setSelectionColor(alpha(CYAN, 110));
            setSelectedTextColor(Color.WHITE);
            setFont(police(Font.PLAIN, 15));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    focus.vers(1);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focus.vers(0);
                }
            });
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            int w = getWidth();
            int h = getHeight();
            float f = focus.get();
            RoundRectangle2D forme = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 14, 14);
            g2.setColor(isEditable() ? new Color(0x101018) : CARTE_CLAIRE);
            g2.fill(forme);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(melange(BORD, CYAN, f));
            g2.draw(forme);
            if (getText().isEmpty() && exemple != null) {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(alpha(DISCRET, 160));
                g2.drawString(exemple, getInsets().left, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Groupe de pastilles en relief dont une seule est sélectionnée (comme des boutons radio). */
    static class Chips extends JPanel {
        private static final int PROFONDEUR = 4;
        private final List<Chip> chips = new ArrayList<>();
        private int selection = 0;
        private IntConsumer auChangement = i -> { };

        Chips(String... options) {
            super(new FlowLayout(FlowLayout.LEFT, 8, 0));
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            for (int i = 0; i < options.length; i++) {
                Chip chip = new Chip(i, options[i]);
                chips.add(chip);
                add(chip);
            }
            chips.get(0).actif.fixer(1);
        }

        /** Couleur propre à chaque pastille (sinon cyan). */
        Chips couleurs(Color... couleurs) {
            for (int i = 0; i < couleurs.length && i < chips.size(); i++) {
                chips.get(i).couleur = couleurs[i];
            }
            repaint();
            return this;
        }

        void auChangement(IntConsumer action) {
            this.auChangement = action;
        }

        int selection() {
            return selection;
        }

        void selectionner(int index) {
            selection = index;
            for (Chip c : chips) {
                c.actif.vers(c.index == index ? 1 : 0);
            }
            auChangement.accept(index);
        }

        void texte(int index, String texte) {
            chips.get(index).texte = texte;
            chips.get(index).revalidate();
            revalidate();
            repaint();
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        private final class Chip extends JComponent {
            private final int index;
            private String texte;
            private Color couleur = CYAN;
            private final Anim survol = new Anim(this, 0, 0.25f);
            private final Anim actif = new Anim(this, 0, 0.25f);

            Chip(int index, String texte) {
                this.index = index;
                this.texte = texte;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        survol.vers(1);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        survol.vers(0);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        selectionner(Chip.this.index);
                    }
                });
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(police(Font.BOLD, 13));
                return new Dimension(fm.stringWidth(texte) + 30, 36 + PROFONDEUR);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = lisse(g);
                int w = getWidth();
                int h = getHeight();
                float a = actif.get();
                float s = survol.get();
                // sélectionnée = enfoncée et colorée ; sinon sombre et soulevée
                float enfoncement = a * (PROFONDEUR - 1) + (1 - a) * (1.5f - 1.5f * s);
                Color face = melange(melange(CARTE, CARTE_CLAIRE, s), couleur, a);
                Color base = melange(OMBRE, fonce(couleur), a);
                Color bord = a > 0.5f ? null : melange(BORD, couleur, s * 0.7f);
                relief(g2, w, h, PROFONDEUR, enfoncement, face, base, bord, 12);
                g2.setFont(police(Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(melange(melange(DISCRET, TEXTE, s), texteSur(couleur), a));
                float hauteurFace = h - PROFONDEUR;
                g2.drawString(texte, (w - fm.stringWidth(texte)) / 2f, enfoncement + (hauteurFace - fm.getHeight()) / 2f + fm.getAscent());
                g2.dispose();
            }
        }
    }

    /** Barre de progression épaisse, remplie de rayures qui défilent, qui glisse vers sa nouvelle valeur. */
    static class Barre extends JComponent {
        private final Anim valeur = new Anim(this, 0, 0.08f);
        private float decalage = 0;
        private Color couleur = CITRON;

        Barre() {
            setPreferredSize(new Dimension(160, 14));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            new Timer(40, e -> {
                decalage = (decalage + 0.8f) % 16;
                if (valeur.get() > 0) {
                    repaint();
                }
            }).start();
        }

        void valeur(float v) {
            valeur.vers(Math.max(0, Math.min(1, v)));
        }

        void couleur(Color c) {
            couleur = c;
            repaint();
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D piste = new RoundRectangle2D.Float(0, 0, w, h, h, h);
            g2.setColor(new Color(0x101018));
            g2.fill(piste);
            float rempli = w * valeur.get();
            if (rempli > 1) {
                RoundRectangle2D plein = new RoundRectangle2D.Float(0, 0, Math.max(rempli, h), h, h, h);
                g2.setColor(couleur);
                g2.fill(plein);
                Graphics2D g3 = (Graphics2D) g2.create();
                g3.clip(plein);
                g3.setColor(alpha(Color.WHITE, 55));
                for (float x = -h - 16 + decalage; x < rempli + h; x += 16) {
                    Path2D.Float bande = new Path2D.Float();
                    bande.moveTo(x, h);
                    bande.lineTo(x + h, 0);
                    bande.lineTo(x + h + 7, 0);
                    bande.lineTo(x + 7, h);
                    bande.closePath();
                    g3.fill(bande);
                }
                g3.dispose();
            }
            g2.setColor(BORD);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, w - 1.5f, h - 1.5f, h, h));
            g2.dispose();
        }
    }

    /** Étiquette aux coins arrondis, bord et texte de la même couleur. */
    static class Pastille extends JLabel {
        private Color couleur;

        Pastille(String texte, Color couleur) {
            super(texte);
            setFont(police(Font.BOLD, 13));
            setBorder(new EmptyBorder(5, 12, 5, 12));
            couleur(couleur);
        }

        void couleur(Color c) {
            couleur = c;
            setForeground(c);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D forme = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 12, 12);
            g2.setColor(alpha(couleur, 30));
            g2.fill(forme);
            g2.setColor(couleur);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(forme);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Bandeau de message (succès / erreur / info) qui arrive en rebondissant puis disparaît. */
    static class Banniere extends JComponent {
        enum Type { SUCCES, ERREUR, INFO }

        private final Anim apparition = new Anim(this, 0, 0.13f);
        private final Timer masquage = new Timer(3000, e -> cacher());
        private String texte = "";
        private Type type = Type.INFO;

        Banniere() {
            setPreferredSize(new Dimension(200, 54));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            masquage.setRepeats(false);
        }

        /** dureeMs = 0 : reste affiché jusqu'au prochain appel. */
        void montrer(String message, Type nouveauType, int dureeMs) {
            texte = message;
            type = nouveauType;
            apparition.fixer(0);
            apparition.vers(1);
            masquage.stop();
            if (dureeMs > 0) {
                masquage.setInitialDelay(dureeMs);
                masquage.start();
            }
        }

        void cacher() {
            apparition.vers(0);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            float a = apparition.get();
            if (a < 0.01f) {
                return;
            }
            Color c = switch (type) {
                case SUCCES -> VERT;
                case ERREUR -> ROUGE;
                case INFO -> AMBRE;
            };
            Graphics2D g2 = lisse(g);
            opacite(g2, a);
            // petit rebond : on dépasse légèrement puis on revient
            float rebond = (float) Math.sin(a * Math.PI) * 4;
            g2.translate(0, (1 - a) * 18 - rebond);
            int w = getWidth() - 5;
            int h = getHeight() - 9;
            g2.setColor(OMBRE);
            g2.fill(new RoundRectangle2D.Float(5, 5, w, h, 16, 16));
            RoundRectangle2D forme = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 16, 16);
            g2.setColor(CARTE);
            g2.fill(forme);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(forme);

            float cx = 26;
            float cy = h / 2f;
            g2.fill(new RoundRectangle2D.Float(cx - 13, cy - 13, 26, 26, 9, 9));
            switch (type) {
                case SUCCES -> coche(g2, cx, cy, 14, ENCRE);
                case ERREUR -> croix(g2, cx, cy, 22, ENCRE);
                case INFO -> {
                    g2.setColor(ENCRE);
                    g2.fill(new Ellipse2D.Float(cx - 2, cy - 7.5f, 4, 4));
                    g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Line2D.Float(cx, cy - 0.5f, cx, cy + 6));
                }
            }
            g2.setFont(police(Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(TEXTE);
            g2.drawString(tronquer(texte, fm, w - 70), 52, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Gros titre : chaque lettre a sa couleur et ondule doucement, avec une ombre dure. */
    static class Titre extends JComponent {
        private final String texte;
        private final Font font;
        private float t = 0;

        Titre(String texte, float taille) {
            this.texte = texte;
            this.font = policeTitre(taille);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            new Timer(40, e -> {
                t += 0.09f;
                repaint();
            }).start();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(font);
            return new Dimension(fm.stringWidth(texte) + 10, fm.getHeight() + 10);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            float x = 0;
            int couleur = 0;
            for (int i = 0; i < texte.length(); i++) {
                String lettre = String.valueOf(texte.charAt(i));
                float y = fm.getAscent() + 4 + (float) Math.sin(t + i * 0.55) * 3;
                if (!lettre.isBlank()) {
                    g2.setColor(OMBRE);
                    g2.drawString(lettre, x + 4, y + 4);
                    g2.setColor(ARC_EN_CIEL[couleur++ % ARC_EN_CIEL.length]);
                    g2.drawString(lettre, x, y);
                }
                x += fm.stringWidth(lettre);
            }
            g2.dispose();
        }
    }
}
