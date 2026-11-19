# AssetPilot — Architecture et configuration

Mini-projet Architecture SOA et Services Web — 3 GLID G1 — 2026/2027
Encadrante : Dr. Ing. Ghada Feki
Équipe : Mohamed Triki, Omar Ezzeddine, Yessine Koubaa, Ibrahim Ben Abdallah

---

## 1. Architecture

### 1.1 Vue d'ensemble

AssetPilot est composé de trois microservices métier et d'une API Gateway. Chaque service utilise un style d'intégration différent, adapté à son rôle, et possède sa propre base de données.

```
                 Front-end · Postman · Swagger UI
                              │ HTTP/JSON
                              ▼
               ┌──────────────────────────────┐
               │ API Gateway          :8083   │  Spring Cloud Gateway (WebMVC)
               │ routage · CORS · timeouts    │
               └───────┬──────────────┬───────┘
         /api/v1/**    │              │  /ws/**
         /swagger-ui/**│              │
         /v3/api-docs/**▼             ▼
  ┌─────────────────────────┐  SOAP  ┌─────────────────────────┐
  │ Asset Service    :8081  │──────▶ │ Warranty Service :8082  │
  │ REST / OpenAPI          │  XML   │ SOAP / XSD / WSDL       │
  │ base : assetdb          │        │ base : warrantydb       │
  └───────────┬─────────────┘        └─────────────────────────┘
              │ gRPC / Protobuf (HTTP/2)
              ▼
  ┌─────────────────────────┐
  │ Maintenance Service     │
  │ gRPC :9090 · HTTP :8084 │
  │ base : maintenancedb    │
  └─────────────────────────┘
```

### 1.2 Services et choix des styles

| Service | Style | Responsabilités | Justification du style | Responsable |
|---|---|---|---|---|
| **Asset Service** | REST / OpenAPI | Équipements, employés, affectations ; orchestration des appels inter-services | API HTTP versionnée (`/api/v1`), adaptée au CRUD et au client web, documentée par OpenAPI | Yessine Koubaa |
| **Maintenance Service** | gRPC / Protocol Buffers | Demandes de maintenance, planification, suivi des statuts | Communication interne typée et performante ; streaming serveur pour le suivi des statuts | Mohamed Triki |
| **Warranty Service** | SOAP / Spring-WS | Vérification et détails des garanties | Simule un système partenaire externe : contrat formel XSD (contract-first), WSDL, SOAP Faults | Ibrahim Ben Abdallah |
| **API Gateway** | Spring Cloud Gateway | Point d'entrée unique, routage, CORS | Masque la topologie interne aux clients | Omar Ezzeddine |

### 1.3 Flux inter-services

| Flux | Protocole | Opérations | Déclencheur |
|---|---|---|---|
| Asset → Warranty | SOAP 1.1 | `CheckWarranty`, `GetWarrantyDetails` | Consultation de garantie ; vérification avant toute intervention |
| Asset → Maintenance | gRPC unaire | `CreateMaintenanceRequest`, `ScheduleIntervention`, `GetIntervention`, `UpdateInterventionStatus` | Déclaration de panne, planification, changement de statut |
| Asset → Maintenance | gRPC server-streaming | `TrackInterventionStatus` | Suivi en temps réel, relayé au client en Server-Sent Events |

**Scénario principal : déclarer une panne** (`POST /api/v1/equipments/{id}/maintenance-requests`)

1. L'Asset Service vérifie la garantie auprès du Warranty Service (SOAP).
   - Garantie absente : le SOAP Fault `WARRANTY_NOT_FOUND` donne `covered=false`.
   - Service injoignable ou trop lent : repli `status=UNKNOWN`, et le traitement continue.
2. L'Asset Service crée la demande de maintenance (gRPC). Si une date est fournie, il planifie aussi l'intervention.
3. L'équipement passe à `IN_MAINTENANCE`. Il redevient `ASSIGNED` ou `AVAILABLE` quand l'intervention est `COMPLETED` ou `CANCELLED`.

### 1.4 Contrats d'interface

| Contrat | Fichier source | Publication |
|---|---|---|
| OpenAPI 3 (REST) | Généré depuis le code (springdoc) | `GET /v3/api-docs` ; Swagger UI : `/swagger-ui.html` |
| XSD / WSDL (SOAP) | `warranty-service/src/main/resources/xsd/warranty.xsd` | `GET /ws/warranties.wsdl` (WSDL 1.1 généré depuis le XSD) |
| Protobuf (gRPC) | `maintenance-service/src/main/proto/assetpilot/maintenance/v1/maintenance.proto` | Réflexion gRPC activée sur le port 9090 |

**Règles d'évolution :**
- Le `.proto` est versionné par package (`assetpilot.maintenance.v1`). Un numéro de champ n'est jamais réutilisé ; un champ supprimé est déclaré `reserved` ; une rupture de compatibilité passe à `v2`.
- Le XSD porte la version dans son namespace (`http://assetpilot.com/warranty/v1`).
- L'Asset Service contient une copie de ces deux contrats pour générer ses clients :
  - `asset-service/src/main/proto/` pour les stubs gRPC ;
  - `asset-service/src/main/resources/contracts/warranty.xsd` pour les classes JAXB.

  Toute modification d'un contrat doit être recopiée.

### 1.5 Architecture interne des services

Tous les services suivent le même découpage en couches :

| Couche | Asset | Maintenance | Warranty |
|---|---|---|---|
| Exposition | `web/` (contrôleurs REST, `GlobalExceptionHandler`) | `grpc/` (`MaintenanceGrpcService`, `GrpcExceptionAdvice`) | `endpoint/` (`WarrantyEndpoint`) |
| Métier | `service/` | `service/` (transitions de statut, publication des événements) | `service/` |
| Intégration | `client/` (`WarrantySoapClient`, `MaintenanceGrpcClient`) | — | — |
| Persistance | `domain/`, `repository/` (JPA) | `model/`, `repository/` (JPA) | `model/`, `repository/` (JPA) |
| Transverse | `config/`, `dto/`, `exception/` | — | `config/`, `exception/` |

### 1.6 Gestion des erreurs

Les erreurs REST sont renvoyées au format **Problem Details (RFC 7807)**, avec les champs `service` et `remoteCode` quand elles proviennent d'un service distant.

| Origine | Erreur | Statut HTTP renvoyé par l'Asset Service |
|---|---|---|
| Validation | Champ invalide | 400 (+ détail par champ dans `errors`) |
| Métier | Ressource absente / règle violée | 404 / 409 |
| SOAP | Fault `WARRANTY_NOT_FOUND` / `INVALID_WARRANTY_REQUEST` | 404 / 400 |
| SOAP | Connexion impossible / délai dépassé | 503 / 504 |
| gRPC | `NOT_FOUND` / `INVALID_ARGUMENT` / `FAILED_PRECONDITION` | 404 / 400 / 409 |
| gRPC | `UNAVAILABLE` / `DEADLINE_EXCEEDED` | 503 / 504 |

---

## 2. Configuration

### 2.1 Socle technique

| Élément | Version |
|---|---|
| Java (cible de compilation) | 17 |
| Spring Boot (Asset, Maintenance, Warranty) | 3.3.13 |
| Spring Boot / Spring Cloud (Gateway) | 4.1.2 / 2025.1.4 |
| gRPC Java / Protocol Buffers | 1.65.1 / 3.25.8 |
| grpc-spring-boot-starter (net.devh) | 3.1.0.RELEASE |
| springdoc-openapi | 2.6.0 |
| Spring Web Services · WSDL4J · jaxb-maven-plugin | géré par Spring Boot · 1.6.3 · 4.0.8 |
| Base de données | H2 (une instance par service) |
| Build / conteneurs | Maven 3.9 · Docker Compose |

### 2.2 Ports

| Service | Port | Usage |
|---|---|---|
| API Gateway | 8083 | Point d'entrée HTTP |
| Asset Service | 8081 | REST, Swagger UI, console H2 |
| Warranty Service | 8082 | SOAP (`/ws`), WSDL, console H2 |
| Maintenance Service | 9090 | gRPC |
| Maintenance Service | 8084 | HTTP d'administration (Actuator) |

### 2.3 Fichiers de configuration

| Service | Fichier |
|---|---|
| Asset | `asset-service/src/main/resources/application.properties` |
| Maintenance | `maintenance-service/src/main/resources/application.yml` |
| Warranty | `warranty-service/src/main/resources/application.yml` |
| Gateway | `gateway/src/main/resources/application.yaml` |

La configuration est **externalisée** : chaque valeur qui change selon l'environnement est lue depuis une variable d'environnement, avec une valeur par défaut pour le développement local (syntaxe `${VARIABLE:défaut}`).

### 2.4 Variables d'environnement

| Service | Variable | Défaut (local) | Valeur Docker Compose |
|---|---|---|---|
| Asset | `SERVER_PORT` | `8081` | `8081` |
| Asset | `ASSET_DB_URL` | `jdbc:h2:mem:assetdb;DB_CLOSE_DELAY=-1` | `jdbc:h2:file:/data/assetdb` |
| Asset | `WARRANTY_SERVICE_URL` | `http://localhost:8082/ws` | `http://warranty-service:8082/ws` |
| Asset | `MAINTENANCE_GRPC_ADDRESS` | `static://localhost:9090` | `static://maintenance-service:9090` |
| Asset | `GATEWAY_PUBLIC_URL` | `http://localhost:8083` | `http://localhost:8083` |
| Asset | `SEED_DATA` | `true` | (défaut) |
| Maintenance | `SERVER_PORT` | `8084` | `8084` |
| Maintenance | `GRPC_PORT` | `9090` | `9090` |
| Maintenance | `MAINTENANCE_DB_URL` | `jdbc:h2:mem:maintenancedb;DB_CLOSE_DELAY=-1` | `jdbc:h2:file:/data/maintenancedb` |
| Warranty | `SERVER_PORT` | `8082` | `8082` |
| Warranty | `WARRANTY_DB_URL` | `jdbc:h2:mem:warrantydb;…` | (défaut, en mémoire) |
| Warranty | `WARRANTY_DDL_AUTO` | `create-drop` | (défaut) |
| Gateway | `SERVER_PORT` | `8083` | `8083` |
| Gateway | `ASSET_SERVICE_URL` | `http://localhost:8081` | `http://asset-service:8081` |
| Gateway | `WARRANTY_SERVICE_URL` | `http://localhost:8082` | `http://warranty-service:8082` |
| Gateway | `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:4200,http://localhost:5173` | idem |

### 2.5 Configuration des appels inter-services (Asset Service)

| Propriété | Valeur | Rôle |
|---|---|---|
| `assetpilot.warranty.url` | `${WARRANTY_SERVICE_URL}` | URL de l'endpoint SOAP |
| `assetpilot.warranty.connect-timeout` | `2s` | Délai maximal de connexion au Warranty Service |
| `assetpilot.warranty.read-timeout` | `3s` | Délai maximal de réponse SOAP |
| `grpc.client.maintenance.address` | `${MAINTENANCE_GRPC_ADDRESS}` | Adresse du serveur gRPC |
| `grpc.client.maintenance.negotiation-type` | `plaintext` | gRPC sans TLS sur le réseau interne |
| `assetpilot.maintenance.deadline` | `3s` | Deadline de chaque appel gRPC unaire |
| `assetpilot.maintenance.stream-deadline` | `10m` | Durée maximale d'un flux de suivi |
| `spring.autoconfigure.exclude` | `WebServicesAutoConfiguration` | L'Asset Service est seulement client SOAP |
| `server.forward-headers-strategy` | `framework` | Prise en compte des en-têtes `X-Forwarded-*` derrière la Gateway |

Ces propriétés sont chargées dans des classes typées : `WarrantyClientProperties` et `MaintenanceClientProperties` (`@ConfigurationProperties`).

### 2.6 Configuration du serveur gRPC (Maintenance Service)

| Propriété | Valeur | Rôle |
|---|---|---|
| `grpc.server.port` | `${GRPC_PORT:9090}` | Port du serveur gRPC |
| `grpc.server.reflection-service-enabled` | `true` | Découverte des services par grpcurl / Postman |
| `server.port` | `${SERVER_PORT:8084}` | Port HTTP réservé à Actuator |

### 2.7 Configuration SOAP (Warranty Service)

Dans `WebServiceConfig` :
- servlet `MessageDispatcherServlet` sur `/ws/*` ;
- WSDL 1.1 généré par `DefaultWsdl11Definition` (port type `WarrantyPort`, namespace `http://assetpilot.com/warranty/v1`) ;
- `DetailSoapFaultDefinitionExceptionResolver`, qui ajoute un élément `ServiceFault` (code, message, assetId, horodatage) dans le `<detail>` des SOAP Faults.

### 2.8 Configuration de la Gateway

| Route | Prédicat | Destination |
|---|---|---|
| `asset-rest` | `Path=/api/v1/**` | `${ASSET_SERVICE_URL}` |
| `asset-openapi` | `Path=/v3/api-docs/**,/swagger-ui/**,/swagger-ui.html` | `${ASSET_SERVICE_URL}` |
| `warranty-soap` | `Path=/ws/**` | `${WARRANTY_SERVICE_URL}` |

Autres réglages :
- délai de connexion vers les services : `3s` ;
- **CORS** centralisé dans `CorsConfig` : origines configurables ; méthodes GET, POST, PUT, PATCH, DELETE et OPTIONS ; en-tête `Location` exposé. Les services en aval n'ajoutent pas d'en-têtes CORS, ce qui évite les doublons.

### 2.9 Bases de données

| Service | Base | Mode local | Mode Docker | Schéma |
|---|---|---|---|---|
| Asset | `assetdb` | H2 en mémoire | H2 fichier, volume `asset-data` | `ddl-auto: update` |
| Maintenance | `maintenancedb` | H2 en mémoire | H2 fichier, volume `maintenance-data` | `ddl-auto: update` |
| Warranty | `warrantydb` | H2 en mémoire | H2 en mémoire | `create-drop` + jeu de garanties chargé au démarrage |

Aucun service n'accède à la base d'un autre. Le lien entre les données passe par l'identifiant métier `assetTag` (ex. `AST-001`), commun aux trois services.

### 2.10 Supervision

Chaque service expose Spring Boot Actuator :

| Endpoint | Services |
|---|---|
| `/actuator/health` (avec sondes `liveness` / `readiness`) | Tous |
| `/actuator/info` | Tous |
| `/actuator/gateway/routes` | Gateway |

### 2.11 Conteneurisation

- **Dockerfile multi-stage** pour chaque service :
  - étape 1 : `maven:3.9-eclipse-temurin-17` (build) ;
  - étape 2 : `eclipse-temurin:17-jre-jammy` (exécution), avec un utilisateur non root.
- **Docker Compose** (`docker-compose.yml`) :
  - réseau interne `assetpilot-net` ;
  - volumes nommés pour les données ;
  - healthchecks sur `/actuator/health` ;
  - ordre de démarrage piloté par `depends_on: condition: service_healthy` : Warranty et Maintenance, puis Asset, puis Gateway.

### 2.12 Lancement

| Mode | Commande |
|---|---|
| Script Windows | `start.bat` (menu Docker / local) · `stop.bat` |
| Script Linux / macOS | `./scripts/start.sh docker` ou `local` · `./scripts/start.sh stop` |
| Docker Compose | `docker compose up --build` |
| Maven, par service | `mvn spring-boot:run` (ordre : Warranty, Maintenance, Asset, Gateway) |

URLs de vérification : http://localhost:8083/actuator/health · http://localhost:8083/swagger-ui.html · http://localhost:8083/ws/warranties.wsdl
