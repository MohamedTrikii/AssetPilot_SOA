# Étape 1 : compiler le projet avec Maven et Java 17
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Dépendances en cache (couche réutilisée tant que le pom.xml ne change pas)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Code source (et contrats .proto / .xsd) puis construction du JAR
# Les tests sont exécutés séparément (mvn verify) avant la construction de l'image
COPY src ./src
RUN mvn -B -q clean package -DskipTests


# Étape 2 : image d'exécution légère (JRE uniquement)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Utilisateur non root
RUN groupadd --system app && useradd --system --gid app app

COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

EXPOSE 8083

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
