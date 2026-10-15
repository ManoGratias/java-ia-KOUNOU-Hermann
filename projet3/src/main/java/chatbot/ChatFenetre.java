package chatbot;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

/**
 * Version fenêtrée (Swing) du chatbot. Elle ne connaît que l'interface AiClient :
 * Mock ou HTTP, la fenêtre fonctionne pareil.
 */
public class ChatFenetre extends JFrame {

    private final AiClient client;

    private final JTextArea conversation = new JTextArea();
    private final JTextField saisie = new JTextField();
    private final JButton envoyer = new JButton("Envoyer");
    private final JLabel statut = new JLabel(" ");

    public ChatFenetre(AiClient client, String mode) {
        super("Chatbot - tuteur Java (" + mode + ")");
        this.client = client;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        conversation.setEditable(false);
        conversation.setLineWrap(true);
        conversation.setWrapStyleWord(true);
        conversation.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        conversation.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton reset = new JButton("Nouvelle conversation");
        reset.addActionListener(e -> reinitialiser());
        JButton save = new JButton("Enregistrer...");
        save.addActionListener(e -> enregistrer());
        JPanel haut = new JPanel(new FlowLayout(FlowLayout.LEFT));
        haut.add(reset);
        haut.add(save);
        haut.add(statut);

        JPanel bas = new JPanel(new BorderLayout(6, 0));
        bas.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        bas.add(saisie, BorderLayout.CENTER);
        bas.add(envoyer, BorderLayout.EAST);

        add(haut, BorderLayout.NORTH);
        add(new JScrollPane(conversation), BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);

        envoyer.addActionListener(e -> envoyer());
        saisie.addActionListener(e -> envoyer());
        // À l'ouverture, le curseur doit être dans le champ de saisie, pas dans la conversation
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                saisie.requestFocusInWindow();
            }
        });

        if (client instanceof MockAiClient) {
            ajouter("Info", "Variable AI_API_KEY absente : réponses factices (MockAiClient).");
        }
        setSize(700, 520);
        setLocationRelativeTo(null);
    }

    private void envoyer() {
        String question = saisie.getText().trim();
        if (question.isEmpty()) {
            return;
        }
        ajouter("Toi", question);
        saisie.setText("");
        occupe(true);

        // L'appel réseau peut prendre plusieurs secondes : on le fait hors du thread Swing,
        // sinon la fenêtre se fige. done() est ensuite rappelé sur le thread Swing.
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return client.ask(question);
            }

            @Override
            protected void done() {
                try {
                    ajouter("IA", get());
                } catch (ExecutionException e) {
                    String message = e.getCause() instanceof AiException
                            ? e.getCause().getMessage()
                            : "Erreur inattendue : " + e.getCause();
                    ajouter("Erreur", message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                occupe(false);
            }
        }.execute();
    }

    private void occupe(boolean enCours) {
        envoyer.setEnabled(!enCours);
        saisie.setEnabled(!enCours);
        statut.setText(enCours ? "L'IA réfléchit..." : " ");
        if (!enCours) {
            saisie.requestFocusInWindow();
        }
    }

    private void ajouter(String auteur, String texte) {
        conversation.append(auteur + " > " + texte + "\n\n");
        conversation.setCaretPosition(conversation.getDocument().getLength()); // défile en bas
    }

    private void reinitialiser() {
        client.reset();
        conversation.setText("");
        statut.setText("Historique effacé.");
    }

    private void enregistrer() {
        JFileChooser choix = new JFileChooser();
        choix.setSelectedFile(new File("conversation.txt"));
        if (choix.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(choix.getSelectedFile().toPath(), conversation.getText(), StandardCharsets.UTF_8);
            statut.setText("Enregistré : " + choix.getSelectedFile().getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Sauvegarde impossible : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        String cle = System.getenv("AI_API_KEY");
        AiClient client;
        String mode;
        if (cle == null || cle.isBlank()) {
            client = new MockAiClient();
            mode = "hors-ligne";
        } else {
            client = new HttpAiClient(cle.trim(), Main.MESSAGE_SYSTEME);
            mode = "Groq";
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // apparence par défaut
            }
            new ChatFenetre(client, mode).setVisible(true);
        });
    }
}
