package taches;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Version fenêtrée (Swing) du gestionnaire de tâches.
 * Elle réutilise GestionnaireTaches et FichierTaches SANS les modifier : c'est tout l'intérêt
 * d'avoir gardé la logique sans aucun System.out.
 */
public class TachesFenetre extends JFrame {

    private static final Path FICHIER = Path.of("taches.txt");

    private final GestionnaireTaches gestionnaire = new GestionnaireTaches();
    private final FichierTaches fichier = new FichierTaches(FICHIER);

    private final DefaultTableModel modele = new DefaultTableModel(
            new String[] {"N°", "Faite", "Titre", "Priorité", "Échéance"}, 0) {
        @Override
        public boolean isCellEditable(int ligne, int colonne) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int colonne) {
            return colonne == 1 ? Boolean.class : Object.class; // la colonne "Faite" s'affiche en case à cocher
        }
    };
    private final JTable table = new JTable(modele);

    private final JTextField champTitre = new JTextField(18);
    private final JComboBox<Priorite> champPriorite = new JComboBox<>(Priorite.values());
    private final JTextField champEcheance = new JTextField(9);
    private final JLabel statut = new JLabel(" ");

    public TachesFenetre() {
        super("Mes tâches");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitter();
            }
        });

        champPriorite.setSelectedItem(Priorite.MOYENNE);
        // Le thème Windows calcule mal la largeur de la liste ("MOYEN...") : on la fixe.
        champPriorite.setPreferredSize(new java.awt.Dimension(110, champPriorite.getPreferredSize().height));
        champEcheance.setToolTipText("AAAA-MM-JJ, vide = aucune");

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);

        add(creerFormulaire(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(creerBoutons(), BorderLayout.EAST);
        statut.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statut, BorderLayout.SOUTH);

        setSize(780, 420);
        setLocationRelativeTo(null);
        charger();
    }

    private JPanel creerFormulaire() {
        JPanel panneau = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panneau.setBorder(BorderFactory.createTitledBorder("Nouvelle tâche"));
        panneau.add(new JLabel("Titre"));
        panneau.add(champTitre);
        panneau.add(new JLabel("Priorité"));
        panneau.add(champPriorite);
        panneau.add(new JLabel("Échéance"));
        panneau.add(champEcheance);
        JButton ajouter = new JButton("Ajouter");
        ajouter.addActionListener(e -> ajouter());
        champTitre.addActionListener(e -> ajouter());
        champEcheance.addActionListener(e -> ajouter());
        panneau.add(ajouter);
        return panneau;
    }

    private JPanel creerBoutons() {
        JPanel panneau = new JPanel(new GridLayout(0, 1, 0, 6));
        panneau.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panneau.add(bouton("Marquer comme faite", this::marquerFaite));
        panneau.add(bouton("Supprimer", this::supprimer));
        panneau.add(bouton("Supprimer les terminées", this::supprimerTerminees));
        panneau.add(bouton("Trier par priorité", () -> { gestionnaire.trierParPriorite(); rafraichir(); }));
        panneau.add(bouton("Trier par date", () -> { gestionnaire.trierParDate(); rafraichir(); }));
        panneau.add(bouton("Tâches en retard", this::enRetard));
        panneau.add(bouton("Enregistrer", this::sauvegarder));
        JPanel conteneur = new JPanel(new BorderLayout());
        conteneur.add(panneau, BorderLayout.NORTH);
        return conteneur;
    }

    private static JButton bouton(String texte, Runnable action) {
        JButton b = new JButton(texte);
        b.addActionListener(e -> action.run());
        return b;
    }

    /** Recopie la liste du gestionnaire dans le tableau affiché. */
    private void rafraichir() {
        modele.setRowCount(0);
        List<Tache> taches = gestionnaire.lister();
        for (int i = 0; i < taches.size(); i++) {
            Tache t = taches.get(i);
            modele.addRow(new Object[] {
                i + 1,
                t.estFaite(),
                t.getTitre(),
                t.getPriorite(),
                t.getEcheance() == null ? "" : t.getEcheance().toString()
            });
        }
        int retard = gestionnaire.enRetard(LocalDate.now()).size();
        statut.setText(taches.size() + " tâche(s)" + (retard > 0 ? " - " + retard + " en retard" : ""));
    }

    private void charger() {
        try {
            for (Tache t : fichier.charger()) {
                gestionnaire.ajouter(t);
            }
            if (!fichier.getAvertissements().isEmpty()) {
                JOptionPane.showMessageDialog(this, String.join("\n", fichier.getAvertissements()),
                        "Lignes ignorées", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            erreur("Impossible de lire " + FICHIER + " : " + e.getMessage());
        }
        rafraichir();
    }

    private boolean sauvegarder() {
        try {
            fichier.sauvegarder(gestionnaire.lister());
            statut.setText("Enregistré dans " + FICHIER.toAbsolutePath());
            return true;
        } catch (IOException e) {
            erreur("Erreur de sauvegarde : " + e.getMessage());
            return false;
        }
    }

    private void quitter() {
        if (sauvegarder()) {
            dispose();
            return;
        }
        int choix = JOptionPane.showConfirmDialog(this, "La sauvegarde a échoué. Quitter quand même ?",
                "Quitter", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private void ajouter() {
        try {
            LocalDate echeance = GestionnaireTaches.parserDate(champEcheance.getText());
            Priorite priorite = (Priorite) champPriorite.getSelectedItem();
            gestionnaire.ajouter(new Tache(champTitre.getText(), priorite, echeance));
            champTitre.setText("");
            champEcheance.setText("");
            champTitre.requestFocusInWindow();
            rafraichir();
        } catch (TacheInvalideException e) {
            erreur(e.getMessage());
        }
    }

    /** Numéro de la tâche sélectionnée (à partir de 1), ou 0 si aucune. */
    private int numeroSelectionne() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            erreur("Sélectionne d'abord une tâche dans la liste.");
        }
        return ligne + 1;
    }

    private void marquerFaite() {
        int numero = numeroSelectionne();
        if (numero < 1) {
            return;
        }
        try {
            gestionnaire.marquerFaite(numero);
            rafraichir();
        } catch (TacheInvalideException e) {
            erreur(e.getMessage());
        }
    }

    private void supprimer() {
        int numero = numeroSelectionne();
        if (numero < 1) {
            return;
        }
        try {
            gestionnaire.supprimer(numero);
            rafraichir();
        } catch (TacheInvalideException e) {
            erreur(e.getMessage());
        }
    }

    private void supprimerTerminees() {
        int nombre = gestionnaire.supprimerTerminees();
        rafraichir();
        statut.setText(nombre + " tâche(s) terminée(s) supprimée(s).");
    }

    private void enRetard() {
        List<Tache> retard = gestionnaire.enRetard(LocalDate.now());
        if (retard.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune tâche en retard.");
            return;
        }
        StringBuilder texte = new StringBuilder();
        for (Tache t : retard) {
            texte.append("- ").append(t).append('\n');
        }
        JOptionPane.showMessageDialog(this, texte.toString(), retard.size() + " tâche(s) en retard",
                JOptionPane.WARNING_MESSAGE);
    }

    private void erreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Attention", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // apparence par défaut
            }
            new TachesFenetre().setVisible(true);
        });
    }
}
