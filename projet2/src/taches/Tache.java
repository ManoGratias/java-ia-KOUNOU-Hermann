package taches;

import java.time.LocalDate;

/** Une tâche : titre, priorité, état (faite ou non) et date d'échéance facultative. */
public class Tache {

    private final String titre;
    private final Priorite priorite;
    private final LocalDate echeance; // null = pas d'échéance
    private boolean faite;

    public Tache(String titre, Priorite priorite, LocalDate echeance) throws TacheInvalideException {
        if (titre == null || titre.trim().isEmpty()) {
            throw new TacheInvalideException("Le titre ne peut pas être vide.");
        }
        if (priorite == null) {
            throw new TacheInvalideException("La priorité est obligatoire.");
        }
        this.titre = titre.trim();
        this.priorite = priorite;
        this.echeance = echeance;
        this.faite = false;
    }

    public String getTitre() {
        return titre;
    }

    public Priorite getPriorite() {
        return priorite;
    }

    public LocalDate getEcheance() {
        return echeance;
    }

    public boolean estFaite() {
        return faite;
    }

    public void marquerFaite() {
        this.faite = true;
    }

    /** En retard = pas encore faite et échéance strictement passée. */
    public boolean estEnRetard(LocalDate aujourdhui) {
        return !faite && echeance != null && echeance.isBefore(aujourdhui);
    }

    @Override
    public String toString() {
        String coche = faite ? "[x]" : "[ ]";
        String date = echeance == null ? "" : " (échéance " + echeance + ")";
        return coche + " " + titre + " - " + priorite + date;
    }
}
