package taches;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Version fenêtrée du gestionnaire de tâches, thème sombre animé.
 * Elle réutilise GestionnaireTaches et FichierTaches SANS les modifier : c'est tout l'intérêt
 * d'avoir gardé la logique sans aucun System.out.
 */
public class TachesFenetre extends Ui.Fenetre {

    private static final Path FICHIER = Path.of("taches.txt");
    private static final DateTimeFormatter DATE_COURTE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter DATE_LONGUE = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

    private static final int TOUTES = 0;
    private static final int A_FAIRE = 1;
    private static final int FAITES = 2;
    private static final int EN_RETARD = 3;

    private final GestionnaireTaches gestionnaire = new GestionnaireTaches();
    private final FichierTaches fichier = new FichierTaches(FICHIER);

    private final Ui.Champ champTitre = new Ui.Champ("Nouvelle tâche…");
    private final Ui.Chips choixPriorite = new Ui.Chips("Basse", "Moyenne", "Haute");
    private final Ui.Champ champEcheance = new Ui.Champ("AAAA-MM-JJ");
    private final Ui.Bouton ajouter = new Ui.Bouton("+  Ajouter", Ui.JAUNE);

    private final Ui.Chips filtres = new Ui.Chips("Toutes", "À faire", "Faites", "En retard");
    private final JLabel resume = Ui.texte("", 15, Font.BOLD, Ui.TEXTE);
    private final Ui.Barre barre = new Ui.Barre();
    private final JPanel liste = Ui.colonne();
    private final Ui.Banniere message = new Ui.Banniere();

    /** Dernière tâche ajoutée : seule sa carte apparaît en fondu. */
    private Tache derniereAjoutee;

    public TachesFenetre() {
        super("Mes tâches", 1000, 720, null);

        JPanel contenu = new JPanel(new BorderLayout(0, 18));
        contenu.setOpaque(false);
        contenu.setBorder(new EmptyBorder(6, 34, 18, 34));
        contenu.add(creerEntete(), BorderLayout.NORTH);
        contenu.add(creerListe(), BorderLayout.CENTER);
        contenu.add(message, BorderLayout.SOUTH);
        contenu(contenu);

        getRootPane().setDefaultButton(ajouter);
        charger();
    }

    @Override
    void fermer() {
        sauvegarder();
        super.fermer();
    }

    // ------------------------------------------------------------------ construction

    private JPanel creerEntete() {
        // Titre + date du jour à gauche, avancement à droite
        String aujourdhui = LocalDate.now().format(DATE_LONGUE);
        aujourdhui = Character.toUpperCase(aujourdhui.charAt(0)) + aujourdhui.substring(1);
        JPanel titre = Ui.colonne(
                Ui.texte(aujourdhui.toUpperCase(Locale.FRENCH), 12, Font.BOLD, Ui.CYAN),
                new Ui.Titre("Mes tâches", 50));

        barre.setPreferredSize(new Dimension(260, 8));
        JPanel avancement = Ui.colonne(resume, Ui.espace(10), barre);
        avancement.setPreferredSize(new Dimension(260, 50));

        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        haut.add(titre, BorderLayout.WEST);
        JPanel droite = new JPanel(new GridBagLayout());
        droite.setOpaque(false);
        droite.add(avancement);
        haut.add(droite, BorderLayout.EAST);

        // Carte d'ajout
        choixPriorite.couleurs(Ui.VERT, Ui.AMBRE, Ui.ROUGE);
        choixPriorite.selectionner(1);
        champEcheance.setColumns(8);
        ajouter.addActionListener(e -> ajouter());

        Ui.Carte carte = new Ui.Carte(new GridBagLayout());
        carte.setBorder(new EmptyBorder(16, 18, 16, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 14);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        carte.add(champTitre, c);
        c.weightx = 0;
        carte.add(choixPriorite, c);
        carte.add(champEcheance, c);
        c.insets = new Insets(0, 0, 0, 0);
        carte.add(ajouter, c);

        JPanel entete = new JPanel(new BorderLayout(0, 18));
        entete.setOpaque(false);
        entete.add(haut, BorderLayout.NORTH);
        entete.add(carte, BorderLayout.SOUTH);
        return entete;
    }

    private JPanel creerListe() {
        filtres.auChangement(i -> rafraichir());

        Ui.Bouton triPriorite = Ui.Bouton.fantome("Trier : priorité", 13);
        triPriorite.addActionListener(e -> {
            gestionnaire.trierParPriorite();
            sauvegarder();
            rafraichir();
        });
        Ui.Bouton triDate = Ui.Bouton.fantome("Trier : date", 13);
        triDate.addActionListener(e -> {
            gestionnaire.trierParDate();
            sauvegarder();
            rafraichir();
        });
        Ui.Bouton nettoyer = Ui.Bouton.fantome("Supprimer les terminées", 13);
        nettoyer.addActionListener(e -> {
            int n = gestionnaire.supprimerTerminees();
            sauvegarder();
            rafraichir();
            message.montrer(n + " tâche(s) terminée(s) supprimée(s).", Ui.Banniere.Type.INFO, 2500);
        });

        JPanel barreOutils = new JPanel(new BorderLayout());
        barreOutils.setOpaque(false);
        barreOutils.add(filtres, BorderLayout.WEST);
        barreOutils.add(Ui.ligne(8, triPriorite, triDate, nettoyer), BorderLayout.EAST);

        liste.setBorder(new EmptyBorder(4, 0, 4, 6));
        JPanel conteneur = new JPanel(new BorderLayout());
        conteneur.setOpaque(false);
        conteneur.add(liste, BorderLayout.NORTH); // en haut, sans s'étirer
        JScrollPane defilement = Ui.defilement(conteneur);

        JPanel panneau = new JPanel(new BorderLayout(0, 14));
        panneau.setOpaque(false);
        panneau.add(barreOutils, BorderLayout.NORTH);
        panneau.add(defilement, BorderLayout.CENTER);
        return panneau;
    }

    // ------------------------------------------------------------------ actions

    private void charger() {
        try {
            for (Tache t : fichier.charger()) {
                gestionnaire.ajouter(t);
            }
            int ignorees = fichier.getAvertissements().size();
            if (ignorees > 0) {
                message.montrer(ignorees + " ligne(s) corrompue(s) ignorée(s) dans " + FICHIER, Ui.Banniere.Type.ERREUR, 6000);
            }
        } catch (IOException e) {
            message.montrer("Impossible de lire " + FICHIER + " : " + e.getMessage(), Ui.Banniere.Type.ERREUR, 0);
        }
        rafraichir();
    }

    /** Sauvegarde après chaque modification : rien n'est perdu même si l'appli plante. */
    private void sauvegarder() {
        try {
            fichier.sauvegarder(gestionnaire.lister());
        } catch (IOException e) {
            message.montrer("Erreur de sauvegarde : " + e.getMessage(), Ui.Banniere.Type.ERREUR, 0);
        }
    }

    private void ajouter() {
        try {
            LocalDate echeance = GestionnaireTaches.parserDate(champEcheance.getText());
            Priorite priorite = Priorite.values()[choixPriorite.selection()];
            Tache tache = new Tache(champTitre.getText(), priorite, echeance);
            gestionnaire.ajouter(tache);
            derniereAjoutee = tache;
            sauvegarder();
            champTitre.setText("");
            champEcheance.setText("");
            champTitre.requestFocusInWindow();
            if (filtres.selection() == FAITES || filtres.selection() == EN_RETARD && !tache.estEnRetard(LocalDate.now())) {
                filtres.selectionner(TOUTES); // sinon la nouvelle tâche serait cachée par le filtre
            } else {
                rafraichir();
            }
            message.montrer("Tâche ajoutée : " + tache.getTitre(), Ui.Banniere.Type.SUCCES, 2500);
        } catch (TacheInvalideException e) {
            message.montrer(e.getMessage(), Ui.Banniere.Type.ERREUR, 3500);
        }
    }

    private void marquerFaite(int numero) {
        try {
            gestionnaire.marquerFaite(numero);
            sauvegarder();
            rafraichir();
            message.montrer("Bien joué !", Ui.Banniere.Type.SUCCES, 1800);
        } catch (TacheInvalideException e) {
            message.montrer(e.getMessage(), Ui.Banniere.Type.ERREUR, 3000);
        }
    }

    private void supprimer(int numero) {
        try {
            Tache t = gestionnaire.supprimer(numero);
            sauvegarder();
            rafraichir();
            message.montrer("Supprimée : " + t.getTitre(), Ui.Banniere.Type.INFO, 2500);
        } catch (TacheInvalideException e) {
            message.montrer(e.getMessage(), Ui.Banniere.Type.ERREUR, 3000);
        }
    }

    /** Reconstruit la liste affichée à partir du gestionnaire, selon le filtre choisi. */
    private void rafraichir() {
        LocalDate aujourdhui = LocalDate.now();
        List<Tache> taches = gestionnaire.lister();
        int faites = 0;
        for (Tache t : taches) {
            if (t.estFaite()) {
                faites++;
            }
        }
        int retard = gestionnaire.enRetard(aujourdhui).size();

        filtres.texte(TOUTES, "Toutes  " + taches.size());
        filtres.texte(A_FAIRE, "À faire  " + (taches.size() - faites));
        filtres.texte(FAITES, "Faites  " + faites);
        filtres.texte(EN_RETARD, "En retard  " + retard);
        resume.setText(taches.isEmpty() ? "Aucune tâche" : faites + " / " + taches.size() + " terminées");
        barre.valeur(taches.isEmpty() ? 0 : (float) faites / taches.size());

        liste.removeAll();
        int filtre = filtres.selection();
        int affichees = 0;
        for (int i = 0; i < taches.size(); i++) {
            Tache t = taches.get(i);
            boolean visible = switch (filtre) {
                case A_FAIRE -> !t.estFaite();
                case FAITES -> t.estFaite();
                case EN_RETARD -> t.estEnRetard(aujourdhui);
                default -> true;
            };
            if (visible) {
                liste.add(new CarteTache(t, i + 1, t == derniereAjoutee));
                liste.add(Ui.espace(10));
                affichees++;
            }
        }
        if (affichees == 0) {
            String vide = switch (filtre) {
                case A_FAIRE -> "Plus rien à faire. Profite !";
                case FAITES -> "Aucune tâche terminée pour l'instant.";
                case EN_RETARD -> "Rien en retard, bravo !";
                default -> "Aucune tâche. Ajoute la première juste au-dessus.";
            };
            JLabel label = Ui.texte(vide, 17, Font.PLAIN, Ui.DISCRET);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(new EmptyBorder(60, 0, 0, 0));
            label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            liste.add(label);
        }
        derniereAjoutee = null;
        liste.revalidate();
        liste.repaint();
    }

    // ------------------------------------------------------------------ une tâche à l'écran

    /** Carte d'une tâche, entièrement dessinée : rond à cocher, titre, pastilles, bouton supprimer. */
    private final class CarteTache extends JComponent {
        private static final int HAUTEUR = 70;
        private static final int OMBRE = 5;
        private final Tache tache;
        private final int numero;
        private final Ui.Anim survol = new Ui.Anim(this, 0, 0.2f);
        private final Ui.Anim survolCroix = new Ui.Anim(this, 0, 0.25f);
        private final Ui.Anim apparition;

        CarteTache(Tache tache, int numero, boolean nouvelle) {
            this.tache = tache;
            this.numero = numero;
            this.apparition = new Ui.Anim(this, nouvelle ? 0 : 1, 0.12f);
            apparition.vers(1);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setToolTipText(tache.getTitre());

            MouseAdapter souris = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    survol.vers(1);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    survol.vers(0);
                    survolCroix.vers(0);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    boolean surCroix = zoneCroix().contains(e.getPoint());
                    survolCroix.vers(surCroix ? 1 : 0);
                    setCursor(Cursor.getPredefinedCursor(surCroix || !tache.estFaite()
                            ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (zoneCroix().contains(e.getPoint())) {
                        supprimer(CarteTache.this.numero);
                    } else if (!tache.estFaite()) {
                        marquerFaite(CarteTache.this.numero);
                    }
                }
            };
            addMouseListener(souris);
            addMouseMotionListener(souris);
        }

        private Rectangle zoneCroix() {
            return new Rectangle(getWidth() - OMBRE - 50, (HAUTEUR - OMBRE) / 2 - 16, 32, 32);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, HAUTEUR);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, HAUTEUR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Ui.lisse(g);
            float a = apparition.get();
            float s = survol.get();
            Ui.opacite(g2, a);
            g2.translate((1 - a) * -24, 0);
            int w = getWidth() - OMBRE;
            int h = HAUTEUR - OMBRE;
            boolean faite = tache.estFaite();
            LocalDate aujourdhui = LocalDate.now();
            Color couleurPriorite = switch (tache.getPriorite()) {
                case HAUTE -> Ui.ROUGE;
                case MOYENNE -> Ui.AMBRE;
                case BASSE -> Ui.VERT;
            };

            // fond de la carte avec ombre dure ; au survol elle se soulève un peu
            float lev = 2 * s;
            g2.setColor(Ui.OMBRE);
            g2.fill(new RoundRectangle2D.Float(OMBRE, OMBRE, w, h, 18, 18));
            g2.translate(-lev, -lev);
            RoundRectangle2D forme = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 18, 18);
            g2.setColor(faite ? new Color(0x15151F) : Ui.melange(Ui.CARTE, Ui.CARTE_CLAIRE, s));
            g2.fill(forme);
            g2.setColor(Ui.melange(Ui.BORD, faite ? Ui.BORD : couleurPriorite, s));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(forme);
            // bloc de priorité à gauche
            g2.setColor(faite ? Ui.alpha(couleurPriorite, 80) : couleurPriorite);
            g2.fill(new RoundRectangle2D.Float(1, 1, 10, h - 2, 8, 8));
            g2.fillRect(6, 1, 6, h - 2);

            // rond à cocher
            float cx = 42;
            float cy = h / 2f;
            if (faite) {
                g2.setColor(Ui.VERT);
                g2.fill(new RoundRectangle2D.Float(cx - 13, cy - 13, 26, 26, 9, 9));
                Ui.coche(g2, cx, cy, 14, Ui.ENCRE);
            } else {
                g2.setColor(Ui.alpha(Ui.VERT, (int) (60 * s)));
                g2.fill(new RoundRectangle2D.Float(cx - 13, cy - 13, 26, 26, 9, 9));
                g2.setColor(Ui.melange(Ui.DISCRET, Ui.VERT, s));
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(cx - 12, cy - 12, 24, 24, 8, 8));
            }

            // pastilles à droite (de droite à gauche)
            int x = w - 60;
            String priorite = tache.getPriorite().name().charAt(0) + tache.getPriorite().name().substring(1).toLowerCase();
            x = Ui.pastille(g2, priorite, faite ? Ui.DISCRET : couleurPriorite, x, (int) cy) - 8;
            if (tache.getEcheance() != null) {
                String date;
                Color couleurDate;
                if (tache.estEnRetard(aujourdhui)) {
                    date = "En retard · " + tache.getEcheance().format(DATE_COURTE);
                    couleurDate = Ui.ROUGE;
                } else if (!faite && tache.getEcheance().equals(aujourdhui)) {
                    date = "Aujourd'hui";
                    couleurDate = Ui.AMBRE;
                } else {
                    date = tache.getEcheance().format(DATE_COURTE);
                    couleurDate = faite ? Ui.DISCRET : Ui.CYAN;
                }
                x = Ui.pastille(g2, date, couleurDate, x, (int) cy) - 8;
            }

            // titre (barré si faite)
            g2.setFont(Ui.police(faite ? Font.PLAIN : Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            String titre = Ui.tronquer(tache.getTitre(), fm, x - 80);
            float ty = cy - fm.getHeight() / 2f + fm.getAscent();
            g2.setColor(faite ? Ui.DISCRET : Ui.TEXTE);
            g2.drawString(titre, 70, ty);
            if (faite) {
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Line2D.Float(70, cy + 1, 70 + fm.stringWidth(titre), cy + 1));
            }

            // bouton supprimer (visible au survol de la carte)
            float sc = survolCroix.get();
            Rectangle zc = zoneCroix();
            if (sc > 0.01f) {
                g2.setColor(Ui.alpha(Ui.ROUGE, (int) (255 * sc)));
                g2.fill(new RoundRectangle2D.Float(zc.x, zc.y, zc.width, zc.height, 10, 10));
            }
            Graphics2D g3 = (Graphics2D) g2.create();
            Ui.opacite(g3, a * (0.25f + 0.75f * Math.max(s, sc)));
            Ui.croix(g3, (float) zc.getCenterX(), (float) zc.getCenterY(), 26, Ui.melange(Ui.DISCRET, Ui.ENCRE, sc));
            g3.dispose();
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        Ui.demarrer(() -> new TachesFenetre().setVisible(true));
    }
}
