package taches;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Logique métier de la to-do list.
 * Aucun System.out ici : cette classe ne sait pas COMMENT on affiche, elle renvoie des données
 * ou lève des exceptions. On pourrait la réutiliser telle quelle avec une interface graphique ou web.
 */
public class GestionnaireTaches {

    private static final Pattern FORMAT_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    // ArrayList plutôt qu'un tableau : sa taille s'adapte quand on ajoute ou supprime.
    private final List<Tache> taches = new ArrayList<>();

    public void ajouter(Tache tache) {
        taches.add(tache);
    }

    /** Renvoie une copie : l'appelant ne peut pas modifier la liste interne par erreur. */
    public List<Tache> lister() {
        return new ArrayList<>(taches);
    }

    public int nombre() {
        return taches.size();
    }

    /** Les numéros commencent à 1 pour l'utilisateur, les indices à 0 pour la liste. */
    public void marquerFaite(int numero) throws TacheInvalideException {
        getParNumero(numero).marquerFaite();
    }

    public Tache supprimer(int numero) throws TacheInvalideException {
        getParNumero(numero);
        return taches.remove(numero - 1);
    }

    /** Défi "sans IA" : supprime toutes les tâches terminées et renvoie combien ont été supprimées. */
    public int supprimerTerminees() {
        int avant = taches.size();
        taches.removeIf(Tache::estFaite);
        return avant - taches.size();
    }

    /** Tri par priorité décroissante (HAUTE d'abord). */
    public void trierParPriorite() {
        taches.sort(Comparator.comparing(Tache::getPriorite).reversed());
    }

    /** Tri par échéance croissante ; les tâches sans date vont à la fin. */
    public void trierParDate() {
        taches.sort(Comparator.comparing(Tache::getEcheance, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    public List<Tache> enRetard(LocalDate aujourdhui) {
        List<Tache> resultat = new ArrayList<>();
        for (Tache t : taches) {
            if (t.estEnRetard(aujourdhui)) {
                resultat.add(t);
            }
        }
        return resultat;
    }

    /** Convertit une saisie "3" en numéro valide, ou lève une exception lisible. */
    public int parserNumero(String saisie) throws TacheInvalideException {
        int numero;
        try {
            numero = Integer.parseInt(saisie.trim());
        } catch (NumberFormatException e) {
            throw new TacheInvalideException("\"" + saisie + "\" n'est pas un numéro.");
        }
        getParNumero(numero);
        return numero;
    }

    /** Valide le format AAAA-MM-JJ avec une regex, puis que la date existe vraiment (pas de 2026-02-30). */
    public static LocalDate parserDate(String saisie) throws TacheInvalideException {
        if (saisie == null || saisie.trim().isEmpty()) {
            return null;
        }
        String texte = saisie.trim();
        if (!FORMAT_DATE.matcher(texte).matches()) {
            throw new TacheInvalideException("Date invalide, format attendu : AAAA-MM-JJ.");
        }
        try {
            return LocalDate.parse(texte);
        } catch (java.time.format.DateTimeParseException e) {
            throw new TacheInvalideException("Cette date n'existe pas : " + texte);
        }
    }

    private Tache getParNumero(int numero) throws TacheInvalideException {
        if (numero < 1 || numero > taches.size()) {
            throw new TacheInvalideException("Aucune tâche n°" + numero + " (il y en a " + taches.size() + ").");
        }
        return taches.get(numero - 1);
    }
}
