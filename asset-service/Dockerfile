# Étape 1 : compiler le projet avec Maven et Java 17
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copier le fichier de configuration Maven
COPY pom.xml .

# Télécharger les dépendances
RUN mvn -B dependency:go-offline

# Copier le code source
COPY src ./src

# Compiler et générer le JAR
# Les tests seront exécutés séparément avant cette construction
RUN mvn -B clean package -DskipTests


# Étape 2 : créer l'image d'exécution
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Créer un utilisateur pour exécuter le service
RUN groupadd --system app \
    && useradd --system --gid app app

# Copier le JAR depuis l'étape de compilation
COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

# Port utilisé par Spring Boot
EXPOSE 8081

# Démarrer le service
ENTRYPOINT ["java", "-jar", "app.jar"]