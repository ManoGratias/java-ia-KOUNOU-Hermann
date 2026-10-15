# Échanges avec l'IA

Trace des explications demandées à l'assistant pendant l'atelier (rubriques « 🤖 Avec l'IA » de l'énoncé).

---

## Projet 1 : Quiz

### `Scanner`, `trim()` et `equalsIgnoreCase()`

**`Scanner`** lit ce que l'utilisateur tape au clavier.

```java
Scanner scanner = new Scanner(System.in);
String ligne = scanner.nextLine();   // attend que l'utilisateur appuie sur Entrée
```

**`trim()`** renvoie une copie du texte sans les espaces au début et à la fin (le texte d'origine ne change pas).

```java
"  new  ".trim()   // "new"
```

**`equalsIgnoreCase()`** compare le *contenu* de deux textes en ignorant majuscules et minuscules.

```java
"NEW".equalsIgnoreCase("new")   // true
"NEW".equals("new")             // false
```

Les deux ensemble : `saisie.trim().equalsIgnoreCase(reponse)` accepte `"  New "` pour `"new"`.
À ne pas faire : `saisie == reponse`, qui compare les références (deux objets différents en mémoire), pas le texte.

### Vérification d'une réponse générée

Question générée : « En binaire, combien vaut 1010 en décimal ? », réponse attendue `10`.
Vérification : 1×8 + 0×4 + 1×2 + 0×1 = **10**. Correct.

---

## Projet 2 : Gestionnaire de tâches

### Séparer la logique et l'affichage

- `GestionnaireTaches` contient les règles (ajouter, supprimer, marquer, trier) et **ne fait aucun `System.out`** :
  il renvoie des valeurs ou lève une `TacheInvalideException`.
- `Main` (le menu) est la seule classe qui lit le clavier et affiche.

Pourquoi : la logique devient réutilisable et testable. Preuve dans ce projet : la version fenêtre
(`TachesFenetre`) utilise `GestionnaireTaches` **sans y changer une ligne**. Si la classe affichait elle-même
ses messages dans la console, ils n'apparaîtraient jamais dans une fenêtre.

### `try-with-resources`, `BufferedReader`, `BufferedWriter`

```java
try (BufferedWriter writer = Files.newBufferedWriter(chemin, StandardCharsets.UTF_8)) {
    writer.write("HAUTE;false;;Rendre le TP");
    writer.newLine();
}   // writer.close() est appelé automatiquement ici, même si une exception a été levée
```

- La ressource déclarée entre parenthèses est **fermée automatiquement** à la sortie du bloc. Avec un `try`
  simple, il faut un `finally { writer.close(); }`, et si on l'oublie, les données peuvent rester dans le tampon
  et ne jamais être écrites sur le disque.
- `BufferedWriter` regroupe les écritures en mémoire avant de les envoyer au disque (plus rapide) ; `newLine()`
  écrit le bon retour à la ligne.
- `BufferedReader.readLine()` lit une ligne à la fois et renvoie `null` à la fin du fichier, d'où la boucle
  `while ((ligne = reader.readLine()) != null)`.

### Relecture critique : cas limites

| Cas limite | Traitement dans le code |
| --- | --- |
| Titre vide ou fait d'espaces | refusé par le constructeur de `Tache` (`TacheInvalideException`) |
| Fichier absent (premier lancement) | `Files.exists` : liste vide, pas d'erreur |
| Fichier vide ou lignes vides | ignorées (`isBlank()`) |
| Virgule ou point-virgule dans le titre | titre en dernier, découpage `split(";", 4)` : le titre peut contenir `;` |
| Ligne corrompue (priorité inconnue, champs manquants) | ignorée avec un avertissement au démarrage |
| Date impossible (`2026-02-30`) | regex `AAAA-MM-JJ` puis `LocalDate.parse` : refusée |
| Numéro de tâche `abc` ou `99` | message d'erreur, pas de plantage |
| Accents tapés dans la console Windows | `Scanner` lit avec l'encodage de la console |

---

## Projet 3 : Chatbot

### La requête et la réponse JSON (API Groq, format OpenAI)

Requête : `POST https://api.groq.com/openai/v1/chat/completions` avec deux en-têtes,
`Authorization: Bearer <clé>` (qui suis-je) et `Content-Type: application/json` (le corps est du JSON).

```json
{
  "model": "openai/gpt-oss-20b",
  "messages": [
    { "role": "system", "content": "Tu es un tuteur Java bienveillant..." },
    { "role": "user", "content": "C'est quoi une interface ?" }
  ]
}
```

- `system` : la personnalité du bot ; `user` : mes questions ; `assistant` : ses réponses précédentes.
  Renvoyer tout l'historique à chaque requête permet à l'IA de « se souvenir ».
- Le corps est construit avec `JsonObject` / `JsonArray` de Gson : une question contenant des guillemets
  (`Et un "record" ?`) est échappée correctement, alors qu'un texte collé à la main casserait le JSON.

Réponse : le texte est dans `choices[0].message.content`.

```java
racine.getAsJsonArray("choices").get(0).getAsJsonObject()
      .getAsJsonObject("message").get("content").getAsString();
```

Statuts utiles : **200** OK, **401** clé absente ou invalide, **404** modèle introuvable (le cas de
`llama-3.1-8b-instant`), **429** trop de requêtes, **5xx** problème côté serveur.

### Interface et `MockAiClient`

Une **interface** est un contrat : `AiClient` dit « je sais répondre à `ask(question)` » sans dire comment.
`MockAiClient` et `HttpAiClient` signent ce contrat avec `implements AiClient`.
`Main` ne manipule qu'un `AiClient` : au démarrage, on choisit l'un ou l'autre selon la présence de
`AI_API_KEY`, et le reste du programme ne change pas.

`MockAiClient` est utile pour développer et tester sans clé, sans réseau et sans consommer le quota gratuit,
et pour que le programme reste utilisable si l'API est en panne.
