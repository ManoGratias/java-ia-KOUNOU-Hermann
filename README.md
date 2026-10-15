# java-ia — Atelier Java + IA (B3 IABD)

## Lancer les projets

### Dans Cursor

1. *File > Open Workspace from File…* et choisir `java-ia.code-workspace` (à la racine du dépôt).
   Les trois projets apparaissent dans l'explorateur ; le code est dans `src/`.
2. Attendre quelques secondes que l'extension Java charge les projets (barre d'état en bas).
3. Onglet *Run and Debug* (Ctrl+Shift+D), choisir dans la liste puis **F5** :
   - `Fenêtre - Quiz`, `Fenêtre - Tâches`, `Fenêtre - Chatbot` : versions avec interface ;
   - `Console - Quiz`, `Console - Tâches`, `Console - Chatbot` : versions console (énoncé).

Ou dans le terminal de Cursor (Ctrl+ù), depuis la racine du dépôt :

```powershell
# Quiz
cd projet1; javac -encoding UTF-8 -d out src/quiz/*.java; java -cp out quiz.QuizFenetre   # ou quiz.Quiz

# Tâches
cd projet2; javac -encoding UTF-8 -d out src/taches/*.java; java -cp out taches.TachesFenetre   # ou taches.Main

# Chatbot (console par défaut ; les guillemets sont nécessaires sous PowerShell)
cd projet3; mvn -q compile exec:java "-Dexec.mainClass=chatbot.ChatFenetre"   # ou : mvn -q compile exec:java
```

**Clé API (projet 3)** : coller la clé Groq après `AI_API_KEY=` dans `projet3/.env` (copie de `projet3/.env.example`) :
les lancements *Run and Debug* la lisent automatiquement. Dans un terminal : `$env:AI_API_KEY="gsk_..."` avant `mvn`.
Le `.env` est ignoré par Git. Sans clé, le chatbot fonctionne en mode hors-ligne (`MockAiClient`).

Chaque projet a un `README.md` avec les réponses aux points de contrôle. Les explications demandées à l'IA sont dans
`ECHANGES_IA.md`, le bilan dans `NOTES.md`.

### Bonus : versions avec fenêtres (Swing)

Hors énoncé. Les versions console restent les versions de référence. Chaque projet a aussi une version fenêtrée,
style « arcade » sombre (formes qui flottent, couleurs vives, boutons en relief, animations), entièrement dessinée
en Java2D **sans aucune bibliothèque**. Le style commun est dans `Ui.java` (identique dans les trois projets).
Les fenêtres réutilisent les mêmes classes de logique : `quiz.QuizFenetre`, `taches.TachesFenetre`, `chatbot.ChatFenetre`.

---

# ☕🤖 Atelier Java + IA : 3 mini-projets

**Durée : 3h · En autonomie · Outils : VS Code, IntelliJ ou Cursor**

Aujourd'hui, tu ne regardes plus, tu **produis**. Tu vas construire 3 mini-projets de difficulté croissante avec l'IA comme partenaire.

> **Règle d'or : l'IA t'accompagne, elle ne code pas à ta place.** À la fin de chaque projet, tu dois pouvoir expliquer chaque ligne de ton code, y compris celles écrites par l'IA.

| Projet | Thème                                       | Niveau | Durée indicative |
| ------ | ------------------------------------------- | ------ | ---------------- |
| 1      | Quiz en console                             | 🟢      | 40 min           |
| 2      | Gestionnaire de tâches qui se souvient      | 🟡      | 60 min           |
| 3      | Chatbot : ton programme Java parle à une IA | 🟠      | 60 min           |

Chaque projet a un **socle** ⭐ (obligatoire) et des **extensions** 🚀 (pour ceux qui vont vite).
Si tu débutes en Java, vise le socle des projets 1 et 2, puis attaque le 3 si tu as le temps. C'est un très bon résultat.
Si tu es à l'aise, fais les socles rapidement et passe du temps sur les extensions.

---

## 🧠 La méthode IA en 5 temps (à appliquer à chaque projet)

1. **Comprendre avant de coder.** Demande à l'IA un plan (classes, étapes, pourquoi) **sans code**. Pose des questions sur ce que tu ne comprends pas.
2. **Avancer par petits pas.** Une fonctionnalité à la fois. Ne demande jamais « fais-moi tout le projet ».
3. **Prédire, puis exécuter.** Avant de lancer le programme, écris dans un commentaire ce que tu t'attends à voir. Compare ensuite avec le résultat.
4. **Reformuler.** Explique ton code avec tes mots à l'IA (« voici ce que je crois que ça fait, corrige-moi »).
5. **Te faire interroger.** Demande-lui 3 questions pièges sur ton code et réponds sans regarder.

Prompt de départ conseillé :

```
Je suis étudiant en Bac+3 et je [débute / connais un peu] Java.
Je veux faire [projet]. Ne me donne pas de code pour l'instant :
propose-moi les classes à créer et les étapes, avec une phrase d'explication pour chaque.
Ensuite, guide-moi pas à pas et explique chaque nouveau concept.
```

**Interdits**

- ❌ Copier-coller une réponse de l'IA sans la lire et la tester
- ❌ Écrire ou coller ta **clé API** dans un prompt, dans le code ou dans Git

**Attention** : l'IA se trompe, invente des méthodes et oublie des cas limites. Teste toujours ce qu'elle te donne.

---

## 🛠️ Étape 0 : Mise en place (10 min)

- [x] Dépôt GitHub créé : **`java-ia-NOM-Prenom`**
- [x] Trois dossiers : `projet1/`, `projet2/`, `projet3/`
- [x] Un fichier `NOTES.md` à la racine (voir « Rendu »)
- [x] Un `.gitignore` avant le premier commit :

```
out/
target/
bin/
.idea/
*.iml
.vscode/
.env
*.class
```

---

# 🟢 Projet 1 : Quiz en console

**Notions :** classes, objets, tableaux, boucles, conditions, lecture clavier (Cours 1 et 2)

### ⭐ Socle

- [x] Une classe `Question` avec un énoncé et la réponse attendue
- [x] Un tableau d'au moins **5 questions** sur le thème de ton choix
- [x] Le programme pose les questions une par une et lit la réponse au clavier (`Scanner`)
- [x] La comparaison tolère les espaces en trop et les majuscules (`trim()`, `equalsIgnoreCase()`)
- [x] Score final affiché, avec un message différent selon le résultat (au moins 3 paliers)
- [x] Une saisie vide ne fait pas planter le programme
- [x] Conventions du cours respectées : classes en majuscule, méthodes en minuscule

### 🚀 Extensions

- [x] Possibilité de rejouer sans relancer (`do…while`)
- [x] Questions dans un ordre aléatoire
- [x] Choix de la difficulté ou du thème au démarrage
- [x] Chronomètre : temps total affiché à la fin

### 🤖 Avec l'IA

- [ ] Fais proposer par l'IA les classes à créer **avant** de coder (temps 1)
- [ ] Fais générer tes 5 questions par l'IA, puis **vérifie toi-même** la réponse d'au moins une
- [x] Demande-lui d'expliquer `Scanner`, `trim()` et `equalsIgnoreCase()` avec un exemple chacun

### 🔍 Points de contrôle : tu dois savoir répondre

- Quelle est la différence entre ta classe `Question` et un objet `Question` ?
- Pourquoi utiliser `equalsIgnoreCase()` plutôt que `==` pour comparer deux textes ?
- Que se passe-t-il si ta boucle va jusqu'à l'indice 5 dans un tableau de 5 questions ?

### ✋ Défi sans IA (5 min)

Ajoute **à la main** une 6e question et un nouveau palier de score, sans rien demander à l'IA.

---

# 🟡 Projet 2 : Gestionnaire de tâches qui se souvient

**Notions :** POO, `enum`, `ArrayList`, fichiers, exceptions (Cours 1 à 3)

Une application console de type to-do list. Quand on la ferme puis qu'on la relance, **les tâches sont toujours là**.

### ⭐ Socle

- [x] Une classe `Tache` : titre, priorité, état (faite ou non)
- [x] Un `enum Priorite` avec `BASSE`, `MOYENNE`, `HAUTE`
- [x] Une classe `GestionnaireTaches` qui contient une `ArrayList<Tache>` et la logique (ajouter, supprimer, marquer comme faite). **Elle ne contient aucun `System.out`.**
- [x] Un menu en console : ajouter, lister, marquer comme faite, supprimer, quitter
- [x] **Sauvegarde dans un fichier texte** en quittant, **rechargement** au démarrage
- [x] Premier lancement sans fichier : pas d'erreur
- [x] Les fichiers sont lus et écrits avec `try-with-resources` (ou `finally`) et les `IOException` sont gérées
- [x] Un numéro de tâche invalide (lettres, hors liste) affiche un message sans planter

### 🚀 Extensions

- [x] Date d'échéance, validée avec une **regex** (`AAAA-MM-JJ`)
- [x] Ligne corrompue dans le fichier : ignorée avec un avertissement
- [x] Une exception personnalisée (ex. `TacheInvalideException`)
- [x] Tri par priorité ou par date, et commande « tâches en retard »

### 🤖 Avec l'IA

- [ ] Demande à l'IA de t'expliquer **comment séparer la logique et l'affichage**, et pourquoi, avant de coder
- [ ] Avant d'écrire la sauvegarde, fais-toi expliquer `try-with-resources`, `BufferedReader` et `BufferedWriter`
- [ ] **Chasse au bug** : demande à l'IA de te donner une version de ta sauvegarde/chargement avec **un bug caché**, puis trouve-le sans qu'elle te le dise
- [x] **Relecture critique** : demande-lui quels cas limites tu as oubliés (titre vide, fichier vide, tâche avec une virgule dans le titre…) et corrige-en au moins un

### 🔍 Points de contrôle : tu dois savoir répondre

- Pourquoi `GestionnaireTaches` ne doit-elle contenir aucun `System.out` ?
- Que se passe-t-il au lancement si le fichier de sauvegarde n'existe pas ? Où le gères-tu ?
- Que fait `try-with-resources` que ne fait pas un simple `try` ?
- Pourquoi une `ArrayList` plutôt qu'un tableau simple ici ?

### ✋ Défi sans IA (10 min)

Ajoute **à la main** une commande « supprimer toutes les tâches terminées ».

---

# 🟠 Projet 3 : Chatbot, ton programme Java parle à une IA

**Notions :** interfaces, HTTP, JSON, variables d'environnement, gestion d'erreurs

Tu vas écrire un programme qui envoie ta question à une vraie IA via son API web et affiche sa réponse.

### 🔑 Fournisseur d'IA de l'atelier

> **À compléter par l'enseignant avant la séance**
>
> - Fournisseur :  `Groq (GroqCloud), offre gratuite sans carte bancaire`
> - Où créer sa clé gratuite : `https://console.groq.com/keys, *Create API Key* (clé en `gsk_…`, affichée une seule fois)`
> - URL de l'API : `https://api.groq.com/openai/v1/chat/completions` (POST)
> - Modèle à utiliser : ``llama-3.1-8b-instant`
> - Documentation : `https://console.groq.com/docs`



**Format de la requête** : deux en-têtes HTTP, puis un corps JSON.

```
Authorization: Bearer <ta clé>
Content-Type: application/json
```

json

```json
{  "model": "llama-3.1-8b-instant",  "messages": [    { "role": "system", "content": "Tu es un tuteur Java bienveillant." },    { "role": "user", "content": "C'est quoi une interface ?" }  ]}
```

**Format de la réponse** : le texte de l'IA se trouve dans `choices[0].message.content`.

json

```json
{  "choices": [    { "message": { "role": "assistant", "content": "Une interface, c'est..." } }  ]}
```

> 💡 Construis le corps JSON avec les classes `JsonObject` et `JsonArray` de Gson plutôt qu'en collant des morceaux de texte : les guillemets dans les questions te casseraient la requête.

> ⚠️ **N'envoie jamais de données personnelles** (nom, e-mail, mot de passe…) dans tes questions à une IA en ligne. L'offre gratuite est aussi limitée en nombre de requêtes par minute : un statut `429` veut dire « va moins vite ».

> Pas de clé, ou l'inscription traîne ? Ce n'est pas bloquant : `MockAiClient` te permet de finir tout le projet sans réseau.



Chacun de vous va créer **sa propre clé**. Elle se met dans une **variable d'environnement** `AI_API_KEY`, jamais dans le code. Dans ton IDE, elle se configure dans la configuration de lancement (*Run configuration* dans IntelliJ, `env` dans `launch.json` pour VS Code / Cursor).

### Préparer le projet

Crée `projet3` comme **projet Maven** (IntelliJ : *New Project → Maven* ; VS Code / Cursor : commande « Java: Create Java Project → Maven »). Ajoute Gson dans le `pom.xml` pour lire le JSON :

```xml
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.10.1</version>
</dependency>
```

Pas à l'aise avec Maven ? Demande à l'IA de t'expliquer à quoi sert ce fichier.

### ⭐ Socle

- [x] Une **interface** `AiClient` avec une méthode `String ask(String question)`
- [x] Une classe `MockAiClient` qui répond un texte factice : le programme fonctionne **sans clé ni réseau**
- [x] Une classe `HttpAiClient` qui appelle la vraie API avec `java.net.http.HttpClient`
- [x] Au démarrage : si `AI_API_KEY` existe, on utilise `HttpAiClient`, sinon `MockAiClient` avec un message explicite
- [x] Le texte de la réponse est extrait du JSON avec Gson
- [x] Une boucle de conversation en console, avec la commande `/quit`
- [x] Les erreurs (pas de réseau, clé invalide, statut HTTP différent de 200) affichent un message lisible sans planter
- [x] **Aucune clé API** dans le code

### 🚀 Extensions

- [x] Commandes `/reset` (efface l'historique) et `/save` (écrit la conversation dans un fichier)
- [x] Un **message système** (persona) défini en constante, par exemple : *tuteur Java qui explique sans donner la solution complète*
- [x] L'historique est renvoyé à chaque requête pour que l'IA se souvienne du contexte
- [x] Nouvel essai automatique si l'API répond 429 (trop de requêtes)

### 🤖 Avec l'IA

- [ ] Colle la **documentation de l'API** dans ton assistant et demande-lui d'expliquer la requête et la réponse JSON **avant** de coder
- [x] Demande-lui d'expliquer ce qu'est une interface et pourquoi `MockAiClient` est utile
- [ ] **Teach-back** : explique-lui avec tes mots le trajet complet d'une question (console → `AiClient` → requête HTTP → JSON → affichage) et demande-lui de corriger tes erreurs
- [ ] Demande-lui 3 questions pièges sur ton code et réponds-y sans regarder

### 🔍 Points de contrôle : tu dois savoir répondre

- Pourquoi mettre la clé dans une variable d'environnement plutôt que dans le code ?
- À quoi sert `MockAiClient` et dans quel cas est-il utilisé ?
- Que signifie un statut HTTP 429 ? Et 401 ?
- Où et comment extrais-tu le texte de la réponse JSON ?

### ✋ Défi sans IA (10 min)

Ajoute **à la main** une commande `/help` qui liste les commandes disponibles, ou change le message système pour donner une autre personnalité à ton bot.

---

# 📦 Rendu

- [x] Tout est poussé sur ton dépôt GitHub `java-ia-NOM-Prenom` (dossiers `projet1`, `projet2`, `projet3`)
- [x] Aucune clé API dans ton dépôt (cherche `key` et `sk-` dans tes fichiers)
- [x] Les cases de ce README correspondant à ton travail sont cochées (`[x]`)
- [x] `NOTES.md` complété : pour chaque projet, 3 lignes sur *ce que j'ai compris*, *ce que l'IA a mal fait ou inventé*, *ce que je n'ai pas encore compris*
- [ ] Lien du dépôt envoyé à moi par message privé sur Discord avant ce soir.

### Test final

Clone ton propre dépôt dans un **dossier vide** et lance chaque projet depuis ce clone. Si ça ne marche pas chez toi, ça ne marchera pas chez moi non plus. 😉

*Bon code, et amuse-toi avec ton nouveau coéquipier IA !*
