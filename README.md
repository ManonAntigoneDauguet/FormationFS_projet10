# BobApp

L'application de joke par excellence !

# Table des matières
 - [1 - Installation du projet](#1---installation-du-projet)
    - [1.1 - Côté front-end](#11---front-end)
    - [1.2 - Côté back-end](#12---back-end)
 - [2 - Mise en place de la CI/CD](#2---ci--cd)

<br>

# 1 - Installation du projet
## 1.1 - Front-end 

- `cd front` : aller dans le dossier front
- `npm install` : installer les dépendances
- `npm run start` : lancer l'application front

**Docker**

- `docker build -t bobapp-front .` : build le container
- `docker run -p 4200:4200 --name bobapp-front -d bobapp-front` : démarre le container

## 1.2 - Back-end

- `cd back` : aller dans le dossier back
- `mvn clean install` : installer les dépendances
- `mvn spring-boot:run` : lancer l'application back
- `mvn test` : lancer les tests

**Docker**

- `docker build -t bobapp-back .` : build le container
- `docker run -p 8080:8080 --name bobapp-back -d bobapp-back ` : démarre le container

<br>

# 2 - CI / CD
## 2.1 - Définitions

 - **GitHub Actions** est une plateforme permettant la mise en place d'une **démarche CI/CD** ("Intégration Continue / Déploiement Continu").  
Son fonctionnement repose sur des **"worflows"**, des fichiers YAML stockés dans le dossier `.github/worklows`.  

 - Une **"pipeline"** est la suite des étapes d'une démarche CI/CD, pouvant inclure différents "worflows". Elle va du commit au déploiement.

 - La **CI ("Intégration Continue")**  vise à faciliter le travail collaboratif sur un dépôt de code partagé, en automatisant des vérifications de code à chaque mise à jour du code.  
Elle inclue l'exécution automatique et d’intégration, ainsi que d'éventuelles analyses de qualité, afin de détecter rapidement d’éventuelles régressions.

 - La **CD ("Déploiement Continu")** vise à automatiser la mise en production de nouvelles versions d’une application. Elle s’appuie sur l'intégration continue et permet de livrer rapidement un code testé, validé et déployable.

## 2.2 - Règles CI/CD de BobApp

Pipeline BobApp :  
- Lors d'un push ou d'un pull-request sur `main`ou `develop`, les worflows `ci-front.yml` et `ci-back.yml` s'enclenchent :
   - Les dépendances du projet sont ajoutées.
   - Les tests sont lancés et le taux de couverture est généré. 
   - Si les tests échouent, alors le job en cours s'arrête et l'analyse sonar n'est pas lancée.  
   - L’analyse Sonar est déclenchée uniquement si les tests sont passés avec succès.  
   - La step d'analyse sonar inclue les variables nécessaires à la connexion à SonarCloud et aux deux projets (front et back) du monorepo.
- La fusion d’une pull request vers `main` n'est autorisée que si une analyse sonar est lancée, réussie, et si elle renvoie à GitHub un status de validation (un "status checks"), ceci à la fois pour le projet front et le projet back.
- Lorsqu'une pull request sur `main` est validée et fusionnée, le worflows `cd.yml` s'enclenche :
   - Les images Docker du front et du back sont constuite et déployées.   
   - Cette étape repose sur les Dockerfile situés (généralement) dans les dossiers `./front` et `./back`.
   - La step de login inclue les variables nécessaires à la connexion à Docker Hub.

Ainsi, le nouveau code est analysé. S'il est valide, il peut être ajouté à la branche `main`du projet. Seul un code propre et validé peut être déployé.

## 2.3 - Détails des settings

### Environnement

La connexion à SonarCloud et Docker Hub est permise par des identifiants, renseignées dans les commandes des wirkflows. Ces identifiants ne doivent pas être ajoutés en dur mais protégés par des variables.  
Dans la partie "Settings" de GitHub, un environnement "bobapp" est créé pour contenir ces secrets. Cet environnement est spécifié dans les worflows pour que les secrets soit retrouvés.

### Protection de branche

La branche `main` est protégée. Les règles de protection de la branche sont ajoutées sur GitHub, dans la partie "Branches" des "Settings" du projet, telle qu'illsutrée ci-après.  

Avant de pouvoir merger dans `main`, une pull-request est nécessaire, et un "status checks" postitif est requis. Ces status checks sont renvoyés par SonarQubeCloud après analyse, et les "status checks" de notre projet sont spécifiés dans les settings.  
Il n'est pas possible de bypass ces règles.

![Liste des règles de protection de la branche main](./ci-cd-pictures/settings-protect-main-branche.png)

### Analyse Sonar

La CI/CD de BobApp repose entre autres sur la version gratuite de Sonar Cloud. Du fait d'être sur la version gratuite, nous utilisons la "Quality Gate" par défaut de SonarCloud : **"Sonar Way"**.

**Mise en place** :   
Pour mettre en place ce setting, la première étape est de créer un compte Sonar sur [SonarCloud](https://sonarcloud.io/) et de le relier au compte GitHub.  
Ensuite créez une "organisation". "Analyse new project" pour ajouter un nouveau projet. Parmis ceux proposés vous retrouver celui de votre compte GitHub. ⚠ : mettre en place un **"mono-repo"** pour autoriser à avoir deux projets (un back et un front) sur le même repository GitHub.  
Lors que vous vous rendrez sur vos nouveaux projets, dans "Informations" vous retrouverez son « Project Key » et son « Organisation Key », à renseigner dans le worflows lors du lancement de l'analyse.  
Dans "Administration" vous aurez accès à l'onglet **"Quality Gate"** où vous pourrez définir et choisir une « Quality Gate ». Elle évalue le projet sur un certaines nombre de critères . En définissant ces critères dans SonarCloud via l'interface web, ces règles s'appliquent à toutes les analyses effectuées par tous les développeurs qui travailleront sur le projet.

Dans les settings de GitHub, ajouter une protection via sur la base des "status checks" renvoyés par les analyses Sonar sur chacun des deux projet Sonar ([voir la partie Protection de branche](#protection-de-branche)).

**SONAR_TOKEN** : Dans l'onglet "Sécurité" de SonarCloud vous pourrez générer un token. Copiez-le et ajoutez-le à votre environnement GitHub ([voir la partie Environnement](#environnement)).
Ce token sera à renseigner dans le worflows lors du lancement de l'analyse pour permettre la connexion à SonarCloud.

###  CD et déploiement Docker
<br>



# 3 - Détails des worflows


### Front : tests, build et analyses

### Back : tests, build et analyses

`mvn clean verify` compile le code avant de lancer les tests.  
Cette étape permet de vérifier que le code compile bien et permet de lancer les tests et de générer le fichier rapport de coverage `jacoco.xml` dans le dossier `./target/site/jacoco`.

<br>

# 4 - Métrique initiales

## 4.1 - Analyse Sonar



## 4.2 - “Notes et avis”

