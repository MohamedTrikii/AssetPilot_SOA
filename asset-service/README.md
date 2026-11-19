# AssetPilot — Asset Service (REST)

Microservice principal de gestion du parc informatique (équipements, employés et affectations).
Il orchestre également les communications inter-services :
- **Warranty Service (SOAP)** : vérification de garantie et consultation des contrats via `WebServiceTemplate` (JAXB).
- **Maintenance Service (gRPC)** : création de demandes, planification d'interventions et streaming de statuts via stubs gRPC.

## Génération des stubs et clients (gRPC & SOAP)

L'Asset Service intègre une copie locale des contrats pour générer automatiquement ses stubs clients :
1. **gRPC (Maintenance Service)** : `src/main/proto/assetpilot/maintenance/v1/maintenance.proto`
   - Généré dans `target/generated-sources/protobuf/java` et `target/generated-sources/protobuf/grpc-java`
   - Package : `com.assetpilot.maintenance.proto.v1`
2. **SOAP (Warranty Service)** : `src/main/resources/contracts/warranty.xsd`
   - Généré dans `target/generated-sources/jaxb`
   - Package : `com.assetpilot.asset_service.client.warranty.soap`

### Résolution des imports dans les IDEs (VS Code / IntelliJ IDEA / Eclipse)

Le fichier `pom.xml` intègre `build-helper-maven-plugin` pour enregistrer automatiquement tous les répertoires générés dans les sources de compilation (`compileSourceRoots`).

Pour générer ou rafraîchir les classes dans votre IDE :
```bash
# Génération directe des sources gRPC et JAXB
mvn clean generate-sources

# Ou compilation complète
mvn clean compile
```

Dans **VS Code** :
- Exécutez `mvn clean compile` dans le terminal.
- Ouvrez la palette de commandes (`Ctrl+Shift+P`) > `Java: Clean Java Language Server Workspace` > `Restart and delete`.
- Le projet sera alors synchronisé sans aucune erreur d'import.

## Lancement

```bash
# Lancement local (port 8081 par défaut)
mvn spring-boot:run
```

- Swagger UI : http://localhost:8081/swagger-ui.html
- OpenAPI JSON : http://localhost:8081/v3/api-docs
- Console H2 : http://localhost:8081/h2-console
