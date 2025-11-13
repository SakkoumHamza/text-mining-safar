#  Moteur de recherche des documents Arabe avec Spring Boot et Thymeleaf


Ce projet est un moteur de recherche web pour une collection de documents, construit avec Spring Boot et Thymeleaf. Il utilise l'algorithme **TF-IDF (Term Frequency-Inverse Document Frequency)** pour classer et récupérer les documents les plus pertinents en fonction de la requête d'un utilisateur.

L'application est conçue pour un corpus de documents en langue arabe, avec une interface utilisateur et des exemples de requêtes axés sur des sujets historiques (par exemple, le califat abbasside, les Omeyyades).

![Capture d'écran de l'application](src/main/resources/screenshots/Home.png)

## 🚀 Fonctionnalités

* **Recherche TF-IDF** : Le cœur du moteur de recherche utilise le TF-IDF pour évaluer la pertinence des documents.
* **Interface en arabe** : Une interface utilisateur entièrement en arabe.
* **Suggestions de requêtes** : Fournit des exemples de requêtes pour guider l'utilisateur (par exemple, 'بغداد عاصمة الخلافة العباسية').
* **Statistiques du corpus** : Affiche des statistiques de base sur la collection de documents, telles que le nombre de documents et le nombre de termes indexés.
* **Options avancées** : Inclut un espace réservé pour les "خيارات متقدمة" (Options avancées).

## 🛠️ Stack technique

* **Backend** : [Spring Boot](https://spring.io/projects/spring-boot)
* **Frontend (Moteur de template)** : [Thymeleaf](https://www.thymeleaf.org/)
* **Algorithme de recherche** : TF-IDF (implémenté en Java)

## 🏁 Démarrage

Pour exécuter ce projet localement, vous aurez besoin de Java (JDK) et de [Apache Maven](https://maven.apache.org/) installés.

1.  **Clonez le dépôt**
    ```bash
    git clone https://github.com/sakkoumhamza/text-mining-safar.git
    ```

2.  **Exécutez l'application Spring Boot**
    Vous pouvez l'exécuter en utilisant l'outil de build Maven :
    ```bash
    mvn spring-boot:run
    ```

3.  **Ouvrez l'application**
    Ouvrez votre navigateur et accédez à `http://localhost:8080`.

## 📄 Corpus de documents

Ce projet est préconfiguré pour fonctionner avec un ensemble de documents . Pour utiliser vos propres données :

1.  Placez vos fichiers de documents (par exemple, `.txt`) dans le répertoire de ressources approprié.
2.  Mettez à jour la logique de service dans la classe SearchEngineConfig pour lire et indexer vos nouveaux documents au démarrage.
