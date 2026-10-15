# Projet 1 : Quiz en console

## Structure

| Fichier | Rôle |
| --- | --- |
| `src/quiz/Question.java` | Une question : énoncé, réponse, thème, difficulté, et `estCorrecte()` qui compare la saisie |
| `src/quiz/Quiz.java` | Le programme : tableau de 12 questions, choix du thème/difficulté, mélange, chrono, score, rejouer |

## Lancer

```bash
javac -encoding UTF-8 -d out src/quiz/*.java
java -cp out quiz.Quiz
```

## Points de contrôle

- **Classe `Question` vs objet `Question`** : la classe est le plan (quels champs, quelles méthodes). Un objet est une
  instance concrète créée avec `new`, avec ses propres valeurs (« Quel mot-clé... ? » / « new »).
- **Pourquoi `equalsIgnoreCase()` et pas `==`** : `==` compare les références (deux objets `String` différents en
  mémoire peuvent avoir le même texte et donner `false`). `equalsIgnoreCase()` compare le contenu, en ignorant la casse.
- **Boucle jusqu'à l'indice 5 sur un tableau de 5** : les indices vont de 0 à 4 ; `tableau[5]` lève une
  `ArrayIndexOutOfBoundsException` et le programme plante. D'où `i < QUESTIONS.length`.

## Prédiction (méthode IA, temps 3)

```
// Je m'attends à : thème 1 + difficulté 1 -> 4 questions Java faciles dans un ordre aléatoire,
// "new" tapé "  NEW " doit être accepté, une saisie vide affiche "Pas de réponse".
```

## Bonus : version fenêtre

`QuizFenetre.java` (Swing + Java2D, style dans `Ui.java`) : trois écrans dans un `CardLayout`.
Accueil avec choix du thème/difficulté en pastilles, question avec chrono et barre de progression, carte qui
s'illumine en vert/rouge selon la réponse, résultat avec un anneau de score animé et des confettis à partir de 75 %.
Elle réutilise `Quiz.QUESTIONS`, `Quiz.selectionner()` et `Quiz.messagePalier()` : la logique n'est pas dupliquée.
