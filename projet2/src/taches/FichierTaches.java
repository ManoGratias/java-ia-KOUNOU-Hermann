package taches;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Sauvegarde et rechargement des tâches dans un fichier texte (UTF-8), une tâche par ligne :
 *
 *     PRIORITE;faite;echeance;titre
 *     HAUTE;false;2026-10-20;Rendre le TP Java
 *
 * Le titre est mis en DERNIER et on découpe avec split(";", 4) : il peut donc contenir
 * des ";" ou des "," sans casser le format.
 */
public class FichierTaches {

    private static final String SEPARATEUR = ";";

    private final Path chemin;
    private final List<String> avertissements = new ArrayList<>();

    public FichierTaches(Path chemin) {
        this.chemin = chemin;
    }

    /** Les lignes ignorées au dernier chargement, pour que l'affichage puisse prévenir l'utilisateur. */
    public List<String> getAvertissements() {
        return avertissements;
    }

    public void sauvegarder(List<Tache> taches) throws IOException {
        // try-with-resources : le writer est fermé (et donc vidé sur le disque) même en cas d'exception.
        try (BufferedWriter writer = Files.newBufferedWriter(chemin, StandardCharsets.UTF_8)) {
            for (Tache t : taches) {
                String echeance = t.getEcheance() == null ? "" : t.getEcheance().toString();
                writer.write(t.getPriorite() + SEPARATEUR + t.estFaite() + SEPARATEUR + echeance + SEPARATEUR + t.getTitre());
                writer.newLine();
            }
        }
    }

    /** Premier lancement : pas de fichier, on renvoie simplement une liste vide. */
    public List<Tache> charger() throws IOException {
        avertissements.clear();
        List<Tache> taches = new ArrayList<>();
        if (!Files.exists(chemin)) {
            return taches;
        }

        try (BufferedReader reader = Files.newBufferedReader(chemin, StandardCharsets.UTF_8)) {
            String ligne;
            int numeroLigne = 0;
            while ((ligne = reader.readLine()) != null) {
                numeroLigne++;
                if (ligne.isBlank()) {
                    continue;
                }
                try {
                    taches.add(lireLigne(ligne));
                } catch (TacheInvalideException | IllegalArgumentException e) {
                    avertissements.add("Ligne " + numeroLigne + " ignorée (" + e.getMessage() + ") : " + ligne);
                }
            }
        }
        return taches;
    }

    private Tache lireLigne(String ligne) throws TacheInvalideException {
        String[] morceaux = ligne.split(SEPARATEUR, 4);
        if (morceaux.length != 4) {
            throw new TacheInvalideException("4 champs attendus");
        }
        Priorite priorite = Priorite.valueOf(morceaux[0].trim()); // IllegalArgumentException si inconnue
        String faite = morceaux[1].trim();
        if (!faite.equals("true") && !faite.equals("false")) {
            throw new TacheInvalideException("état invalide");
        }
        LocalDate echeance = GestionnaireTaches.parserDate(morceaux[2]);

        Tache tache = new Tache(morceaux[3], priorite, echeance);
        if (faite.equals("true")) {
            tache.marquerFaite();
        }
        return tache;
    }
}
