package quiz;

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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Boîte à outils graphique maison (Swing + Java2D) : thème sombre, dégradés, coins arrondis, animations.
 * Aucune bibliothèque externe : chaque composant se dessine lui-même dans paintComponent().
 * Ce fichier est identique dans les trois projets (seul le package change).
 */
final class Ui {

    private Ui() {
    }

    // ------------------------------------------------------------------ palette

    static final Color FOND_1 = new Color(0x0F0C29);
    static final Color FOND_2 = new Color(0x2A2358);
    static final Color FOND_3 = new Color(0x13112A);
    static final Color TEXTE = new Color(0xF4F2FF);
    static final Color DISCRET = new Color(0xA9A6CF);
    static final Color VIOLET = new Color(0x8B5CF6);
    static final Color ROSE = new Color(0xEC4899);
    static final Color CYAN = new Color(0x22D3EE);
    static final Color VERT = new Color(0x34D399);
    static final Color ROUGE = new Color(0xFB7185);
    static final Color AMBRE = new Color(0xFBBF24);

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

    // ------------------------------------------------------------------ polices et dessin

    private static final String POLICE = choisirPolice();

    private static String choisirPolice() {
        String[] disponibles = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String voulue : new String[] {"Segoe UI", "Inter", "Helvetica Neue", "Arial"}) {
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
            UIManager.put("ToolTip.background", FOND_2);
            UIManager.put("ToolTip.foreground", TEXTE);
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(alpha(VIOLET, 160)));
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
        barre.setPreferredSize(new Dimension(10, 0));
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
                g2.setColor(alpha(Color.WHITE, isThumbRollover() ? 90 : 50));
                g2.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8));
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
        g2.setStroke(new BasicStroke(taille * 0.16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(p);
    }

    static void croix(Graphics2D g2, float cx, float cy, float taille, Color couleur) {
        float d = taille * 0.32f;
        g2.setColor(couleur);
        g2.setStroke(new BasicStroke(taille * 0.15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new java.awt.geom.Line2D.Float(cx - d, cy - d, cx + d, cy + d));
        g2.draw(new java.awt.geom.Line2D.Float(cx + d, cy - d, cx - d, cy + d));
    }

    /** Dessine une pastille de texte dont le bord DROIT est en xDroite ; renvoie son bord gauche. */
    static int pastille(Graphics2D g2, String texte, Color couleur, int xDroite, int yCentre) {
        g2.setFont(police(Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int largeur = fm.stringWidth(texte) + 22;
        int hauteur = 24;
        int x = xDroite - largeur;
        int y = yCentre - hauteur / 2;
        g2.setColor(alpha(couleur, 40));
        g2.fill(new RoundRectangle2D.Float(x, y, largeur, hauteur, hauteur, hauteur));
        g2.setColor(alpha(couleur, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, largeur - 1, hauteur - 1, hauteur, hauteur));
        g2.setColor(melange(couleur, Color.WHITE, 0.35f));
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
        g2.setPaint(new GradientPaint(0, 0, VIOLET, 64, 64, ROSE));
        g2.fill(new Ellipse2D.Float(2, 2, 60, 60));
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

    /** Fond en dégradé avec trois halos de couleur qui dérivent lentement. */
    static class Fond extends JPanel {
        private float t = 0;

        Fond(LayoutManager layout) {
            super(layout);
            setOpaque(true);
            new Timer(50, e -> {
                t += 0.015f;
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new LinearGradientPaint(0, 0, w, h, new float[] {0f, 0.55f, 1f},
                    new Color[] {FOND_1, FOND_2, FOND_3}));
            g2.fillRect(0, 0, w, h);
            halo(g2, w * (0.18f + 0.08f * (float) Math.sin(t)), h * (0.22f + 0.1f * (float) Math.cos(t * 0.8)), w * 0.45f, VIOLET, 85);
            halo(g2, w * (0.88f + 0.06f * (float) Math.cos(t * 0.7)), h * (0.85f + 0.08f * (float) Math.sin(t * 1.1)), w * 0.42f, ROSE, 55);
            halo(g2, w * (0.78f + 0.1f * (float) Math.sin(t * 0.5)), h * (0.08f + 0.06f * (float) Math.cos(t)), w * 0.3f, CYAN, 40);
            g2.dispose();
        }

        private static void halo(Graphics2D g2, float x, float y, float r, Color c, int a) {
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(x, y), r, new float[] {0f, 1f},
                    new Color[] {alpha(c, a), alpha(c, 0)}));
            g2.fill(new Ellipse2D.Float(x - r, y - r, r * 2, r * 2));
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
            fond.setBorder(BorderFactory.createLineBorder(alpha(Color.WHITE, 35)));
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
                    g2.setPaint(new GradientPaint(0, 0, VIOLET, 14, 14, ROSE));
                    g2.fill(new Ellipse2D.Float(0, (getHeight() - 14) / 2f, 14, 14));
                    g2.dispose();
                }
            };
            logo.setPreferredSize(new Dimension(24, 42));
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
            g2.setColor(fermeture ? alpha(new Color(0xE11D48), (int) (230 * s)) : alpha(Color.WHITE, (int) (25 * s)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            if (fermeture) {
                croix(g2, cx, cy, 22, TEXTE);
            } else {
                g2.setColor(TEXTE);
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new java.awt.geom.Line2D.Float(cx - 5, cy, cx + 5, cy));
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ composants

    /** Carte "verre dépoli" : fond translucide, coins arrondis, bord qui peut s'illuminer. */
    static class Carte extends JPanel {
        private final Anim eclat = new Anim(this, 0, 0.05f);
        private Color couleurEclat = VIOLET;

        Carte(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(24, 26, 24, 26));
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
            RoundRectangle2D forme = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 26, 26);
            g2.setColor(alpha(Color.WHITE, 16));
            g2.fill(forme);
            if (e > 0) {
                g2.setColor(alpha(couleurEclat, (int) (40 * e)));
                g2.fill(forme);
            }
            g2.setPaint(new GradientPaint(0, 0, alpha(Color.WHITE, 22), 0, getHeight() * 0.5f, alpha(Color.WHITE, 0)));
            g2.fill(forme);
            g2.setStroke(new BasicStroke(1f + e * 1.5f));
            g2.setColor(melange(alpha(Color.WHITE, 36), alpha(couleurEclat, 230), e));
            g2.draw(forme);
            g2.dispose();
        }
    }

    /** Bouton arrondi : dégradé (principal) ou translucide (fantôme), animé au survol. */
    static class Bouton extends JButton {
        private final Color c1;
        private final Color c2;
        private final boolean fantome;
        private final Anim survol = new Anim(this, 0, 0.2f);

        Bouton(String texte, Color c1, Color c2) {
            this(texte, c1, c2, false, 14);
        }

        static Bouton fantome(String texte, float taille) {
            return new Bouton(texte, null, null, true, taille);
        }

        private Bouton(String texte, Color c1, Color c2, boolean fantome, float taille) {
            super(texte);
            this.c1 = c1;
            this.c2 = c2;
            this.fantome = fantome;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(police(Font.BOLD, taille));
            setForeground(TEXTE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int v = Math.round(taille * 0.75f);
            setBorder(new EmptyBorder(v, Math.round(taille * 1.5f), v, Math.round(taille * 1.5f)));
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
                opacite(g2, 0.4f);
            }
            int dy = getModel().isPressed() ? 1 : 0;
            RoundRectangle2D forme = new RoundRectangle2D.Float(0.5f, 0.5f + dy, w - 1, h - 1 - dy, h, h);
            if (fantome) {
                g2.setColor(alpha(Color.WHITE, (int) (14 + 22 * s)));
                g2.fill(forme);
                g2.setColor(alpha(Color.WHITE, (int) (45 + 50 * s)));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(forme);
            } else {
                g2.setPaint(new GradientPaint(0, 0, melange(c1, Color.WHITE, s * 0.18f), w, h, melange(c2, Color.WHITE, s * 0.18f)));
                g2.fill(forme);
                g2.setPaint(new GradientPaint(0, 0, alpha(Color.WHITE, 55), 0, h * 0.6f, alpha(Color.WHITE, 0)));
                g2.fill(forme);
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(fantome ? melange(DISCRET, TEXTE, 0.5f + s * 0.5f) : Color.WHITE);
            String t = getText();
            g2.drawString(t, (w - fm.stringWidth(t)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent() + dy);
            g2.dispose();
        }
    }

    /** Champ de saisie arrondi avec texte d'exemple et bord en dégradé quand il a le focus. */
    static class Champ extends JTextField {
        private final String exemple;
        private final Anim focus = new Anim(this, 0, 0.2f);

        Champ(String exemple) {
            this.exemple = exemple;
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setForeground(TEXTE);
            setCaretColor(TEXTE);
            setSelectionColor(alpha(VIOLET, 150));
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
            RoundRectangle2D forme = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 18, 18);
            g2.setColor(alpha(isEditable() ? Color.BLACK : Color.WHITE, isEditable() ? 70 : 10));
            g2.fill(forme);
            g2.setStroke(new BasicStroke(1f + f));
            g2.setPaint(f > 0.01f
                    ? new GradientPaint(0, 0, alpha(VIOLET, (int) (60 + 195 * f)), w, 0, alpha(CYAN, (int) (60 + 195 * f)))
                    : new GradientPaint(0, 0, alpha(Color.WHITE, 45), w, 0, alpha(Color.WHITE, 45)));
            g2.draw(forme);
            if (getText().isEmpty() && exemple != null) {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(alpha(DISCRET, 170));
                g2.drawString(exemple, getInsets().left, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Groupe de pastilles cliquables dont une seule est sélectionnée (comme des boutons radio). */
    static class Chips extends JPanel {
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

        /** Couleur propre à chaque pastille (sinon dégradé violet-rose). */
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
            private Color couleur;
            private final Anim survol = new Anim(this, 0, 0.2f);
            private final Anim actif = new Anim(this, 0, 0.2f);

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
                return new Dimension(fm.stringWidth(texte) + 32, 36);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = lisse(g);
                int w = getWidth();
                int h = getHeight();
                float a = actif.get();
                float s = survol.get();
                RoundRectangle2D forme = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h);
                g2.setColor(alpha(Color.WHITE, (int) (12 + 16 * s)));
                g2.fill(forme);
                g2.setColor(alpha(Color.WHITE, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(forme);
                if (a > 0.01f) {
                    Graphics2D g3 = (Graphics2D) g2.create();
                    opacite(g3, a);
                    if (couleur == null) {
                        g3.setPaint(new GradientPaint(0, 0, VIOLET, w, h, ROSE));
                        g3.fill(forme);
                    } else {
                        g3.setColor(alpha(couleur, 75));
                        g3.fill(forme);
                        g3.setColor(couleur);
                        g3.setStroke(new BasicStroke(1.5f));
                        g3.draw(forme);
                    }
                    g3.dispose();
                }
                g2.setFont(police(Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(melange(DISCRET, Color.WHITE, Math.max(a, s * 0.6f)));
                g2.drawString(texte, (w - fm.stringWidth(texte)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    /** Barre de progression arrondie, remplie d'un dégradé, qui glisse vers sa nouvelle valeur. */
    static class Barre extends JComponent {
        private final Anim valeur = new Anim(this, 0, 0.08f);

        Barre() {
            setPreferredSize(new Dimension(160, 8));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        void valeur(float v) {
            valeur.vers(Math.max(0, Math.min(1, v)));
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
            g2.setColor(alpha(Color.WHITE, 22));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            float rempli = w * valeur.get();
            if (rempli > 1) {
                g2.setPaint(new GradientPaint(0, 0, VIOLET, w, 0, CYAN));
                g2.fill(new RoundRectangle2D.Float(0, 0, Math.max(rempli, h), h, h, h));
            }
            g2.dispose();
        }
    }

    /** Étiquette en forme de pilule colorée. */
    static class Pastille extends JLabel {
        private Color couleur;

        Pastille(String texte, Color couleur) {
            super(texte);
            setFont(police(Font.BOLD, 12));
            setBorder(new EmptyBorder(5, 12, 5, 12));
            couleur(couleur);
        }

        void couleur(Color c) {
            couleur = c;
            setForeground(melange(c, Color.WHITE, 0.4f));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = lisse(g);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D forme = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h);
            g2.setColor(alpha(couleur, 45));
            g2.fill(forme);
            g2.setColor(alpha(couleur, 130));
            g2.draw(forme);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Bandeau de message (succès / erreur / info) qui apparaît en glissant puis disparaît. */
    static class Banniere extends JComponent {
        enum Type { SUCCES, ERREUR, INFO }

        private final Anim apparition = new Anim(this, 0, 0.16f);
        private final Timer masquage = new Timer(3000, e -> cacher());
        private String texte = "";
        private Type type = Type.INFO;

        Banniere() {
            setPreferredSize(new Dimension(200, 48));
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
            g2.translate(0, (1 - a) * 10);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D forme = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 16, 16);
            g2.setColor(alpha(c, 38));
            g2.fill(forme);
            g2.setColor(alpha(c, 150));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(forme);

            float cx = 26;
            float cy = h / 2f;
            g2.setColor(c);
            g2.fill(new Ellipse2D.Float(cx - 12, cy - 12, 24, 24));
            switch (type) {
                case SUCCES -> coche(g2, cx, cy, 14, FOND_1);
                case ERREUR -> croix(g2, cx, cy, 20, FOND_1);
                case INFO -> {
                    g2.setColor(FOND_1);
                    g2.fill(new Ellipse2D.Float(cx - 1.6f, cy - 6.5f, 3.2f, 3.2f));
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new java.awt.geom.Line2D.Float(cx, cy - 0.5f, cx, cy + 5.5f));
                }
            }
            g2.setFont(police(Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(TEXTE);
            g2.drawString(tronquer(texte, fm, w - 64), 50, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Titre dont les lettres sont remplies d'un dégradé violet - rose - cyan. */
    static class TitreDegrade extends JComponent {
        private final String texte;
        private final Font font;

        TitreDegrade(String texte, float taille) {
            this.texte = texte;
            this.font = police(Font.BOLD, taille);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(font);
            return new Dimension(fm.stringWidth(texte) + 4, fm.getHeight());
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
            int w = Math.max(1, fm.stringWidth(texte));
            g2.setPaint(new LinearGradientPaint(0, 0, w, 0, new float[] {0f, 0.5f, 1f}, new Color[] {VIOLET, ROSE, CYAN}));
            g2.drawString(texte, 0, fm.getAscent());
            g2.dispose();
        }
    }
}
