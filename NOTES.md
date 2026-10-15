# NOTES

> Brouillon à relire et à réécrire avec tes propres mots avant le rendu.

## Projet 1 : Quiz en console

- **Ce que j'ai compris** : une classe (`Question`) est un modèle, chaque `new Question(...)` crée un objet ; on compare les textes avec `equalsIgnoreCase()` après `trim()`, jamais avec `==` qui compare les références.
- **Ce que l'IA a mal fait ou inventé** : _(à compléter avec ce que tu observes en relisant / testant)_.
- **Ce que je n'ai pas encore compris** : pourquoi `Scanner.nextInt()` suivi de `nextLine()` « saute » une ligne (j'ai tout lu avec `nextLine()` pour l'éviter).

## Projet 2 : Gestionnaire de tâches

- **Ce que j'ai compris** : `GestionnaireTaches` ne fait que la logique et lève des exceptions, `Main` s'occupe de l'affichage ; `try-with-resources` ferme le fichier automatiquement, même en cas d'erreur.
- **Ce que l'IA a mal fait ou inventé** : dans sa première version du menu, une simple touche Entrée (saisie vide) quittait l'application, et à la fermeture de l'entrée le programme affichait « Choix inconnu » avant de quitter ; corrigé avec un indicateur `entreeFermee`.
- **Ce que je n'ai pas encore compris** : la syntaxe `Comparator.comparing(...).reversed()` et les références de méthode `Tache::estFaite`.

## Projet 3 : Chatbot

- **Ce que j'ai compris** : l'interface `AiClient` permet d'échanger `MockAiClient` et `HttpAiClient` sans toucher au reste ; la clé est lue avec `System.getenv("AI_API_KEY")` et n'est jamais dans le code.
- **Ce que l'IA a mal fait ou inventé** : _(à compléter : par exemple une méthode Gson qui n'existe pas, ou un mauvais chemin dans le JSON de réponse)_.
- **Ce que je n'ai pas encore compris** : _(à compléter)_.
