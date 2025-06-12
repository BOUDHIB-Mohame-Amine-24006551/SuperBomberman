# SuperBomberman

## Lancement du jeu

Sauf indication contraire, le jeu se lance grâce à la classe `BombermanApp` au chemin suivant :  
`SuperBomberman\src\main\java\fr\univ\bomberman\BombermanApp.java`.  
Il est recommandé de le lancer sous l’éditeur de code **IntelliJ** avec le **JDK 24.0.1**.  
Pour toute question supplémentaire veuillez contacter l’un des créateurs de ce jeu par mail ou message Discord.

---

## Fonctionnalité 1 et 2 : Jeu de base et Mode 4 joueurs

Nous avons développé le jeu de base comme indiqué par les consignes du client qui sont les suivantes :  
dans un premier temps, l'application devra permettre de jouer contre un autre joueur "humain" sur la même machine, à tour de rôle.

Le jeu de base se présente par un mode de jeu 1 contre 1 jouable sur le même clavier.

**Pour accéder à ce jeu** :
- Cliquer sur le bouton **Jouer** dans le menu
- Puis **Lancer 2 joueurs**
- Appuyer sur **OK**

**Touches de jeu :**

- Joueur 1 : `ZQSD` (Mouvements) + `ESPACE` (poser les bombes)  
- Joueur 2 : Flèches directionnelles (Mouvements) + `ENTRÉE` (poser les bombes)

**But :** éliminer son adversaire à l'aide de bombes. Le prénom du joueur gagnant est affiché à la fin.

### Mode 4 joueurs (Bataille Royale)

Pour y accéder :  
- Aller sur la page de choix des modes de jeu
- Cliquer sur **Bataille Royale**

**Touches :**

- Joueur 1 : `ZQSD` + `A`
- Joueur 2 : Flèches directionnelles + `ENTRÉE`
- Joueur 3 : `IJKL` + `U`
- Joueur 4 : `8456` + `7`

---

## Fonctionnalité 3 : Thèmes et profils

Le **changement de thème** est disponible uniquement **en jeu**, via la touche `{T}`.  
Deux thèmes sont proposés : `default` (Super Bomberman) et `pokemon`.

**Ajouter un thème personnalisé :**
- Chemin : `SuperBomberman\src\main\resources\fr\univ\bomberman\image`
- Ajouter un dossier avec des images `.png` pour tuiles, sprites, etc.

⚠️ Le dossier `default` **ne doit pas être modifié** (images de base du jeu).

Un **tutoriel sur les thèmes** est disponible via le bouton "Thème" dans le menu des thèmes.

### Profils

- Menu principal → **Sélectionner Profil**, **Créer Profil**, ou **Gestion Complètes**
- Statistiques enregistrées par profil
- Le profil peut être choisi avant de lancer une partie

---

## Fonctionnalité 4 : Joueur contre Ordinateur

Un **mode alternatif** permettant de jouer contre l’ordinateur.

**Accès :**
- Menu → **Jouer** → **Lancer Joueur Vs Bot** → **OK**

Un bouton d’**instructions et tutoriel** est disponible juste en dessous.

**Difficultés disponibles :**
- Facile
- Moyen
- Difficile

**Touches joueur :**
- `ZQSD` (mouvement) + `ESPACE` (poser bombe)

---

## Fonctionnalité 5 : Éditeur de niveaux

Lancer via la classe suivante :  
`SuperBomberman\src\main\java\fr\univ\bomberman\tools\LevelEditorView.java`

### Fonctionnalités :
- Édition d’une carte **15 x 13**
- Types de blocs :
  - **Vide** : sans collision
  - **Cassable** : détruit par bombe
  - **Incassable** : ne peut être détruit
  - **Interdit** : zone non modifiable en auto-remplissage

### Options :
- **Auto-remplissage aléatoire** : génère des blocs destructibles automatiquement
- **Effacer** : réinitialise la carte
- **Sauvegarder / Charger** : gère les niveaux créés

### Intégration dans le jeu :
- Lancer `BombermanApp`
- Aller dans **Jouer** → **Choisir un niveau**
- Choisir le fichier précédemment sauvegardé

---

## Fonctionnalité 6 : Mode “Capture the flag”

Mode multijoueur où chaque joueur protège son drapeau.

**Accès :**
- Menu → Choisir **Capture The Flag**

**But :** capturer le plus de drapeaux.

**Modes :** 2, 3 ou 4 joueurs.

**Touches :**
- Identiques à celles du mode 1v1 ou 4 joueurs selon le nombre de participants

**Déroulement :**
- Chaque joueur pose un drapeau avec `ENTRÉE`
- La partie démarre après que tous les drapeaux ont été posés

---

## Fonctionnalité 7 : Logs

Des logs sont affichés dans la console pour :
- Suivre les actions réalisées
- Déboguer ou améliorer le jeu

Utile pour les développeurs souhaitant contribuer.

---

## Fonctionnalité 8 : Tutoriel et informations

Des **tutoriels interactifs** sont disponibles dans le jeu :
- Pour le **mode Bot**
- Pour le **mode Capture the Flag**

➡️ Accès via les **boutons dédiés** dans les menus.

Bon jeu a tous ! 

> L'équipe SuperBomberman - SAE 2.01 - Groupe 1.7 - Mohamed-Amine Boudhib, Loïc Hernandez, Idris Mekidiche.
