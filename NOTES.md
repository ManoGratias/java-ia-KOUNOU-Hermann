# NOTES

## Projet 1 : Quiz en console

- **Ce que j'ai compris** : une classe (`Question`) est un modèle, chaque `new Question(...)` crée un objet avec ses propres valeurs ; on compare deux textes avec `trim()` + `equalsIgnoreCase()` et jamais avec `==`, qui compare les références et pas le contenu.
- **Ce que l'IA a mal fait ou inventé** : dans la version fenêtre (bonus), l'énoncé de la question n'apparaissait pas : l'IA avait mis une largeur HTML de 640 px sans savoir que Swing convertit les `px` en points (×1,33), ce qui dépassait la carte. Elle avait aussi oublié de donner le focus au champ de réponse après le changement d'écran.
- **Ce que je n'ai pas encore compris** : pourquoi `Scanner.nextInt()` suivi de `nextLine()` « saute » une ligne ; j'ai lu toutes les saisies avec `nextLine()` pour éviter le problème.

## Projet 2 : Gestionnaire de tâches

- **Ce que j'ai compris** : `GestionnaireTaches` ne fait que la logique et signale les problèmes par des exceptions, `Main` s'occupe de l'affichage ; grâce à ça, la version fenêtre a réutilisé la même classe sans la modifier. `try-with-resources` ferme le fichier automatiquement, même en cas d'erreur.
- **Ce que l'IA a mal fait ou inventé** : dans sa première version du menu, une simple touche Entrée quittait l'application ; et sous Windows, une tâche tapée avec des accents (« Café ») était enregistrée « Caf? », car la console envoie du cp850 alors que `Scanner` lisait en UTF-8. Corrigé en lisant le clavier avec l'encodage de la console (`System.console().charset()`).
- **Ce que je n'ai pas encore compris** : la syntaxe `Comparator.comparing(...).reversed()` et les références de méthode comme `Tache::estFaite`.

## Projet 3 : Chatbot

- **Ce que j'ai compris** : l'interface `AiClient` permet d'utiliser `MockAiClient` (sans réseau) ou `HttpAiClient` (vraie API) sans changer le reste du programme ; la clé est lue avec `System.getenv("AI_API_KEY")` et n'est jamais dans le code ni dans Git ; le texte de la réponse est dans `choices[0].message.content`.
- **Ce que l'IA a mal fait ou inventé** : le modèle de l'énoncé (`llama-3.1-8b-instant`) renvoyait une erreur 404 : j'ai listé les modèles disponibles via l'API Groq et choisi `openai/gpt-oss-20b` (validé par l'enseignant). L'IA avait aussi écrit le nom de la classe principale en dur dans le `pom.xml`, si bien que `-Dexec.mainClass=...` était ignoré.
- **Ce que je n'ai pas encore compris** : comment `SwingWorker` renvoie le résultat sur le thread de l'interface graphique, et la différence exacte entre exception vérifiée et non vérifiée dans une interface.
