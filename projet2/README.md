# Projet 2 : Gestionnaire de tâches qui se souvient

## Structure

| Fichier | Rôle |
| --- | --- |
| `Priorite.java` | `enum` : `BASSE`, `MOYENNE`, `HAUTE` |
| `Tache.java` | Titre, priorité, faite ou non, échéance facultative ; refuse un titre vide |
| `TacheInvalideException.java` | Exception personnalisée (titre vide, numéro invalide, date invalide) |
| `GestionnaireTaches.java` | La logique sur une `ArrayList<Tache>` : ajouter, supprimer, marquer, trier, en retard. **Aucun `System.out`.** |
| `FichierTaches.java` | Sauvegarde / chargement dans `taches.txt` avec `try-with-resources` |
| `Main.java` | Le menu console (seule classe qui affiche) |

Format du fichier, une tâche par ligne : `PRIORITE;faite;echeance;titre` (ex. `HAUTE;false;2026-10-20;Rendre le TP`).
Le titre est en dernier et découpé avec `split(";", 4)` : il peut contenir `;` ou `,`.

## Lancer

```bash
javac -encoding UTF-8 -d out src/taches/*.java
java -cp out taches.Main
```

## Points de contrôle

- **Pourquoi aucun `System.out` dans `GestionnaireTaches`** : séparer la logique de l'affichage. La classe peut être
  réutilisée avec une interface graphique ou web, et testée sans console. Elle signale les problèmes par des exceptions,
  c'est `Main` qui décide comment les afficher.
- **Fichier absent au lancement** : `FichierTaches.charger()` teste `Files.exists(chemin)` et renvoie une liste vide.
  Le fichier sera créé à la première sauvegarde (en quittant).
- **`try-with-resources` vs simple `try`** : la ressource déclarée dans les parenthèses est fermée automatiquement à la
  sortie du bloc, même si une exception est levée. Avec un simple `try`, il faut un `finally` qui appelle `close()`.
- **`ArrayList` plutôt qu'un tableau** : on ne connaît pas le nombre de tâches à l'avance ; l'`ArrayList` grandit et
  rétrécit toute seule et offre `add`, `remove`, `removeIf`, `sort`.

## Extensions réalisées

- Date d'échéance validée par la regex `\d{4}-\d{2}-\d{2}` puis par `LocalDate.parse` (refuse `2026-02-30`)
- Ligne corrompue ignorée avec un avertissement au démarrage
- Exception personnalisée `TacheInvalideException`
- Tri par priorité ou par date, commande « tâches en retard »
- Défi : « supprimer toutes les tâches terminées » (`removeIf`)
