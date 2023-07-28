# PayMyBuddy
PayMyBuddy est une application web qui permet aux utilisateurs de se transférer des fonds de manière simple. \
Les utilisateurs peuvent créer un compte, s'ajouter mutuellement et effectuer des transferts de fonds en utilisant \
leur nom d'utilisateur.


## UML

![ModelLogiqueDonnee-UML](https://github.com/Ivano-P/PayMyBuddy/assets/96083465/026aa58e-4912-42c1-9349-95e2825e93ee)

## Modèle relationnel

![ModelLogiqueDonnee-Model relationnel](https://github.com/Ivano-P/PayMyBuddy/assets/96083465/c36e8524-01d2-4e8d-b770-1782233ce8ca)

## Prerequies

- Java 19
- Spring Boot
- Maven
- Lombok
- AssertJ Core
- base de donnée sql
- serveur tomcat

## Dependances

- spring-boot-starter-web
- spring-boot-starter-test
- springdoc-openapi-starter-webmvc-ui
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-oauth2-client
- mysql-connector-j
- spring-boot-starter-thymeleaf\
- spring-boot-starter-data-jpa
- spring-boot-starter-parent

## Instalation et mise en route

1. cloner le repertoire:\
   git clone https://github.com/Ivano-P/PayMyBuddy.git

2. naviger vers le projet dans votre répertoire\
   cd paymybuddy

3. installer les dépendances\
   mvn install

4. configurer la base de donnée mysql

5. configurer les variables d'environnement

5. lancer l'application\
   mvn spring-boot:run

## Usage

Cette application propose une interface utilisateur web simple, la création de compte et la connexion en utilisant \
Spring Security, ainsi que la logique métier et le backend permettant le transfert de fonds entre utilisateurs. \
Il s'agit d'un prototype simple pour une application de partage d'argent, mais elle doit être liée à une API de \
service de paiement comme Paypal ou Stripe pour être pleinement opérationnelle.


## les test

Vous pouvez lancer les test avec la commande: 'mvn clean test' .
