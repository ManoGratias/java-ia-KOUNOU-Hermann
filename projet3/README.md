# Projet 3 : Chatbot, ton programme Java parle à une IA

## Structure

| Fichier | Rôle |
| --- | --- |
| `AiClient.java` | Interface : `String ask(String question)` et `reset()` |
| `MockAiClient.java` | Réponses factices, sans clé ni réseau |
| `HttpAiClient.java` | Appel HTTP à Groq avec `java.net.http.HttpClient`, JSON avec Gson, historique, nouvel essai sur 429 |
| `AiException.java` | Erreur lisible (réseau, 401, 429, 5xx, JSON inattendu) |
| `Main.java` | Boucle de conversation, persona (`MESSAGE_SYSTEME`), commandes `/help` `/reset` `/save` `/quit` |

## Lancer

```bash
mvn -q compile exec:java
```

Sans variable `AI_API_KEY`, le programme utilise `MockAiClient` et le dit au démarrage.
Avec la clé : `$env:AI_API_KEY="gsk_..."` (PowerShell) avant `mvn`, ou dans `projet3/.env` pour Cursor
(`.env` est dans le `.gitignore`).

## Trajet d'une question

console (`Scanner`) → `Main` → `client.ask()` → `HttpAiClient` ajoute `{role:user}` à l'historique → corps JSON
construit avec `JsonObject`/`JsonArray` → `POST` avec les en-têtes `Authorization: Bearer ...` et
`Content-Type: application/json` → réponse JSON → `choices[0].message.content` lu avec `JsonParser` → affichage.

## Points de contrôle

- **Clé dans une variable d'environnement** : le code est poussé sur GitHub ; une clé écrite dedans est publique
  et peut être volée et utilisée à tes frais. La variable reste sur ta machine.
- **`MockAiClient`** : utilisé quand `AI_API_KEY` est absente. Il permet de développer et tester la boucle et les
  commandes sans réseau ni quota, et montre l'intérêt de l'interface.
- **429** : trop de requêtes (limite de l'offre gratuite) ; le client réessaie jusqu'à 3 fois en respectant
  `Retry-After`. **401** : non authentifié, clé absente, fausse ou révoquée.
- **Extraction du texte** : `HttpAiClient.extraireTexte()` :
  `racine.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString()`.

## Bonus : version fenêtre

`ChatFenetre.java` (Swing). L'appel à l'IA est fait dans un `SwingWorker`, hors du thread graphique : sans ça, la
fenêtre se figerait pendant les secondes d'attente de la réponse. Elle ne connaît que l'interface `AiClient`.

```bash
mvn -q compile exec:java -Dexec.mainClass=chatbot.ChatFenetre
```
