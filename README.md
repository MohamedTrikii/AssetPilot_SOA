# AssetPilot

Plateforme de gestion du parc informatique — Mini-projet Architecture SOA et Services Web.

**Encadrante :** Dr. Ing. Ghada Feki
**Classe :** 3 GLID G1 — 2026/2027

## Équipe

| Membre | Service | Technologie |
|---|---|---|
| Yessine Koubaa | Asset Service | REST / OpenAPI |
| Mohamed Triki | Maintenance Service | gRPC / Protocol Buffers |
| Ibrahim Ben Abdallah | Warranty Service | SOAP / XSD / WSDL |
| Omar Ezzeddine | API Gateway | Spring Cloud Gateway |

## Architecture

```
                 Front-end / Postman / Swagger UI
                              │  HTTP (CORS)
                              ▼
                ┌───────────────────────────┐
                │  API Gateway  :8083       │  /api/v1/**, /swagger-ui/**, /v3/api-docs/** → Asset
                │  Spring Cloud Gateway MVC │  /ws/**                                     → Warranty
                └─────────────┬─────────────┘
                              ▼
                ┌───────────────────────────┐   SOAP (CheckWarranty,      ┌──────────────────────┐
                │  Asset Service  :8081     │──── GetWarrantyDetails) ───▶│ Warranty Service     │
                │  REST /api/v1 + OpenAPI   │                             │ :8082  SOAP/XSD/WSDL │
                │  PostgreSQL assetdb       │   gRPC (unaire + stream)    └──────────────────────┘
                │                           │────────────────────────────▶┌──────────────────────┐
                └───────────────────────────┘                             │ Maintenance Service  │
                                                                          │ :9090 gRPC  :8084 HC │
                                                                          └──────────────────────┘
```

- **Asset Service** : équipements, employés, affectations ; orchestre les appels vers Warranty et Maintenance.
- **Maintenance Service** : demandes de maintenance, planification et suivi des interventions (streaming).
- **Warranty Service** : vérification et consultation des garanties (système partenaire simulé).
- **API Gateway** : point d'entrée unique, routage, CORS.

Détail complet de l'architecture et de la configuration : [docs/architecture-configuration.md](docs/architecture-configuration.md).

Chaque service possède sa propre base de données (PostgreSQL en Docker, H2 en mémoire en local et en test), dont le schéma est versionné par Flyway — voir [Bases de données](docs/architecture-configuration.md#29-bases-de-données). Contrats : [`maintenance.proto`](maintenance-service/src/main/proto/assetpilot/maintenance/v1/maintenance.proto), [`warranty.xsd`](warranty-service/src/main/resources/xsd/warranty.xsd), OpenAPI généré (`/v3/api-docs`). L'Asset Service embarque une copie de ces deux contrats pour générer ses clients (à resynchroniser en cas de modification).

### Flux principal : déclarer une panne

`POST /api/v1/equipments/{id}/maintenance-requests`

1. **Asset → Warranty (SOAP `CheckWarranty`)** : vérification de la couverture. SOAP Fault `WARRANTY_NOT_FOUND` → `covered=false, status=NOT_FOUND`. Service injoignable ou trop lent → repli `status=UNKNOWN, verified=false` (la demande est quand même créée).
2. **Asset → Maintenance (gRPC `CreateMaintenanceRequest`)**, puis `ScheduleIntervention` si `scheduledAt` est fourni. Deadline de 3 s par appel.
3. L'équipement passe à `IN_MAINTENANCE` ; il redevient `ASSIGNED`/`AVAILABLE` quand l'intervention est `COMPLETED` ou `CANCELLED`.

## Lancement

### Scripts de lancement (le plus simple)

| Système | Démarrer | Arrêter |
|---|---|---|
| Windows | double-clic sur `start.bat` (menu Docker / local) | `stop.bat` (`stop.bat reset` efface aussi les données Docker) |
| Windows (ligne de commande) | `start.bat docker` ou `start.bat local` | `stop.bat` |
| Linux / macOS / Git Bash | `./scripts/start.sh docker` ou `./scripts/start.sh local` | `./scripts/start.sh stop` |

Le script vérifie les prérequis et les ports, démarre les services dans le bon ordre en attendant les healthchecks, puis ouvre Swagger UI. En mode local sous Windows, chaque service s'ouvre dans sa propre fenêtre où l'on voit ses logs.

### Option 1 — Docker Compose (recommandé)

```bash
docker compose up --build
```

L'ordre de démarrage est géré par les healthchecks (bases PostgreSQL → Warranty + Maintenance → Asset → Gateway). Les données des trois services sont conservées dans les volumes `asset-db-data`, `warranty-db-data` et `maintenance-db-data` (`docker compose down -v` pour repartir de zéro). Les identifiants des bases se changent dans un fichier `.env` (modèle : [`.env.example`](.env.example)).

### Option 2 — En local avec Maven (4 terminaux, Java 17+)

```bash
cd warranty-service    && mvn spring-boot:run
cd maintenance-service && mvn spring-boot:run
cd asset-service       && mvn spring-boot:run
cd gateway             && mvn spring-boot:run
```

### Tests

```bash
mvn verify   # dans chaque dossier de service
```

| Service | Tests |
|---|---|
| asset-service | REST (CRUD, validation, règles métier, OpenAPI) + flux inter-services (mock SOAP, serveur gRPC in-process) |
| maintenance-service | gRPC unaire, streaming, statuts d'erreur, deadline |
| warranty-service | Endpoint SOAP, validation XSD, SOAP Fault |
| gateway | Chargement du contexte |

## Ports et URLs

| Composant | URL |
|---|---|
| **API Gateway (front-end)** | http://localhost:8083 |
| Swagger UI (via Gateway) | http://localhost:8083/swagger-ui.html |
| OpenAPI JSON | http://localhost:8083/v3/api-docs |
| WSDL (via Gateway) | http://localhost:8083/ws/warranties.wsdl |
| Asset Service direct | http://localhost:8081 (Swagger : `/swagger-ui.html` ; console H2 `/h2-console` en mode local uniquement) |
| Warranty Service direct | http://localhost:8082/ws |
| Maintenance Service gRPC | localhost:9090 (réflexion activée) |
| Healthchecks | `/actuator/health` sur 8081, 8082, 8083, 8084 |

## Tester

- **Swagger UI** : http://localhost:8083/swagger-ui.html (choisir le serveur « Via l'API Gateway »).
- **Postman** : importer [`postman/AssetPilot.postman_collection.json`](postman/AssetPilot.postman_collection.json). Les dossiers s'exécutent dans l'ordre avec le Runner ; les IDs créés sont chaînés automatiquement.
- **gRPC direct** (grpcurl ou Postman > New > gRPC, réflexion serveur) :

```bash
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext -d '{"asset_id":"AST-001","description":"Test"}' \
  localhost:9090 assetpilot.maintenance.v1.MaintenanceService/CreateMaintenanceRequest
grpcurl -plaintext -d '{"intervention_id":"<id>"}' \
  localhost:9090 assetpilot.maintenance.v1.MaintenanceService/TrackInterventionStatus
```

### Données de démonstration

| id | assetTag | Garantie (Warranty Service) |
|---|---|---|
| 1 | AST-001 | ACTIVE / PREMIUM — affecté à l'employé 1 |
| 2 | AST-002 | EXPIRED |
| 3 | AST-003 | ACTIVE / EXTENDED |
| 4 | AST-004 | VOIDED |
| 5 | AST-005 | aucune → SOAP Fault `WARRANTY_NOT_FOUND` |

## API pour le front-end

Base URL : `http://localhost:8083/api/v1` — JSON. Le CORS est configuré sur la Gateway pour `http://localhost:3000`, `:4200` et `:5173` (variable `CORS_ALLOWED_ORIGINS`).

| Méthode | Endpoint | Description |
|---|---|---|
| GET / POST | `/equipments` (`?status=&type=`) | Liste / création |
| GET / PUT / DELETE | `/equipments/{id}` | Détail / mise à jour / suppression |
| GET / POST | `/employees` | Liste / création |
| GET / PUT / DELETE | `/employees/{id}` | Détail / mise à jour / suppression |
| GET / POST | `/assignments` (`?employeeId=&equipmentId=&status=`) | Liste / affectation |
| PATCH | `/assignments/{id}/return` | Restitution |
| GET | `/equipments/{id}/warranty` | Couverture de garantie (SOAP) |
| GET | `/equipments/{id}/warranty/details` | Contrat de garantie (SOAP) |
| POST | `/equipments/{id}/maintenance-requests` | Déclarer une panne (SOAP + gRPC) |
| POST | `/maintenance-requests/{requestId}/interventions` | Planifier une intervention |
| GET | `/interventions/{id}` | Détail d'une intervention |
| PATCH | `/interventions/{id}/status` | `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| GET | `/interventions/{id}/status-stream` | Suivi en temps réel (Server-Sent Events) |

Énumérations : `EquipmentType` = LAPTOP, DESKTOP, MONITOR, PRINTER, SERVER, PHONE, TABLET, NETWORK, OTHER · `EquipmentStatus` = AVAILABLE, ASSIGNED, IN_MAINTENANCE, RETIRED (seuls AVAILABLE et RETIRED se positionnent à la main).

Les erreurs suivent le format Problem Details (RFC 7807) :

```json
{ "status": 409, "title": "Erreur du service maintenance-service",
  "detail": "Transition interdite : SCHEDULED -> COMPLETED (autorisées : [IN_PROGRESS, CANCELLED])",
  "service": "maintenance-service", "remoteCode": "FAILED_PRECONDITION" }
```

Erreurs de validation (400) : champ `errors` = `{ "<champ>": "<message>" }`. Statuts gRPC traduits en HTTP : NOT_FOUND→404, INVALID_ARGUMENT→400, FAILED_PRECONDITION→409, UNAVAILABLE→503, DEADLINE_EXCEEDED→504.

Suivi temps réel côté navigateur :

```js
const es = new EventSource(`http://localhost:8083/api/v1/interventions/${id}/status-stream`);
es.addEventListener('status', e => console.log(JSON.parse(e.data))); // { previousStatus, currentStatus, changedAt }
```

## Technologies

Java 17, Spring Boot 3.3.x (Gateway : Spring Boot 4.1 / Spring Cloud 2025.1), Maven, Spring Cloud Gateway, REST/OpenAPI (springdoc), gRPC (net.devh grpc-spring-boot-starter), Spring Web Services, PostgreSQL 16, H2, Flyway, Docker Compose.

## Branches

- `main` : version stable
- `develop` : intégration
- `feature/*` : une branche par service
