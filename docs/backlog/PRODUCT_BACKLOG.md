# Product Backlog — AssetPilot

> Fichier **généré** depuis [backlog.json](backlog.json) par `node scripts/backlog/backlog.mjs render` — ne pas modifier à la main.
> Comment ajouter un élément ou un membre : [README.md](README.md).

## Vue d’ensemble

- **44** éléments, **165** points — **24** terminés (83 pts, 50 %)
- Statuts : ⚪ Backlog (5) · 🔵 À faire (7) · 🟡 En cours (6) · 🟣 En revue (2) · 🔴 Bloquée (0) · 🟢 Terminée (24)

## Équipe et charge

| Membre | GitHub | Rôle | Éléments | Points | Terminés |
|---|---|---|---:|---:|---:|
| Mohamed Triki | @MohamedTrikii | Maintenance Service (gRPC) | 29 | 111 | 15 |
| Omar Ezzeddine | @oyr1234 | API Gateway | 16 | 56 | 5 |
| Ibrahim Ben Abdallah | @Ibrahim5501 | Warranty Service (SOAP) | 22 | 80 | 11 |
| Yessine Koubaa | @yesinekoubaa | Asset Service (REST) | 27 | 108 | 12 |

_Un élément affecté à plusieurs membres est une tâche collective ; le premier nommé est le responsable principal._

## Sprints

| Sprint | Période | Objectif | Éléments | Points |
|---|---|---|---:|---:|
| **Sprint 0 – Préparation** | 2026-10-27 → 2026-11-14 | Dépôt GitHub organisé, branches initialisées, backlog et répartition validés, structures de base des composants. | 2 | 4 |
| **Sprint 1 – Architecture & REST** | 2026-11-15 → 2026-11-22 | Architecture et configuration initiales ; Asset Service REST avec OpenAPI ; contrats XSD et .proto ; Gateway initialisée. | 10 | 33 |
| **Sprint 2 – SOAP, gRPC & appels** | 2026-11-23 → 2026-11-29 | Opérations SOAP et SOAP Fault ; RPC gRPC unaire et streaming ; appels interservices réels. | 10 | 40 |
| **Sprint 3 – Intégration & qualité** | 2026-11-30 → 2026-12-06 | Routes Gateway finalisées, intégration des composants, tests unitaires et d'intégration. | 7 | 23 |
| **Sprint 4 – Docker & résilience** | 2026-12-07 → 2026-12-13 | Dockerfiles multi-stage, Docker Compose, configuration externalisée, healthchecks, Actuator et résilience. | 8 | 28 |
| **Sprint 5 – Finalisation** | 2026-12-14 → 2026-12-30 | Tests finaux, documentation, rapport de 8 à 12 pages, collection de tests et démonstration. | 7 | 37 |

## Couverture de la grille d’évaluation

| Critère | Barème | Éléments | Terminés | Restants |
|---|---:|---|---:|---|
| C1 — Conception et découpage en services | 3 | US-004, US-020, US-021, US-022, US-023, US-024, US-037 | 5/7 | US-024, US-037 |
| C2 — Service(s) REST | 3 | US-007, US-008, US-009, US-010, US-011, US-012 | 5/6 | US-012 |
| C3 — Service gRPC | 2.5 | US-005, US-013, US-014, US-015, US-016, US-021 | 6/6 | ✅ |
| C4 — Service SOAP | 2.5 | US-006, US-017, US-018, US-019, US-020 | 5/5 | ✅ |
| C5 — Docker et orchestration | 3 | US-028, US-029, US-036, BUG-001 | 3/4 | US-029 |
| C6 — Cloud-native et résilience | 3 | US-030, US-031, US-032, US-036, US-043 | 1/5 | US-030, US-031, US-032, US-043 |
| C7 — Qualité du code et tests | 1.5 | US-001, US-002, US-003, US-025, US-026, US-038, US-039, US-041 | 2/8 | US-003, US-025, US-026, US-038, US-039, US-041 |
| C8 — Documentation et soutenance | 1.5 | US-027, US-033, US-034, US-035, US-037 | 0/5 | US-027, US-033, US-034, US-035, US-037 |
| BONUS — Bonus (jusqu’à +2, à démontrer) | 2 | US-039, US-040, US-041, US-042, US-043 | 0/5 | US-039, US-040, US-041, US-042, US-043 |

## Backlog par épic

### EP-01 — Organisation du projet et dépôt GitHub

Dépôt structuré, stratégie de branches, backlog et suivi Agile sur GitHub Projects.  
Composant : Infra / Docker / CI · 3 éléments · 7 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-001 | Initialiser le dépôt GitHub | Tâche technique | Must have | 2 | S0 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟢 Terminée | B01 |
| US-002 | Définir la stratégie de branches et les règles de Pull Request | Tâche technique | Must have | 2 | S0 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟢 Terminée | B02 |
| US-003 | Mettre en place le Product Backlog et le tableau GitHub Projects | Tâche technique | Must have | 3 | S1 | Mohamed Triki | 🟣 En revue | B01, B02 |

### EP-02 — Architecture et contrats interservices

Architecture microservices, contrats OpenAPI, .proto et XSD/WSDL versionnés.  
Composant : Documentation / Livrables · 5 éléments · 13 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-004 | Définir l'architecture et les flux interservices | Tâche technique | Must have | 3 | S1 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟢 Terminée | B03 |
| US-005 | Définir le contrat .proto versionné du Maintenance Service | Tâche technique | Must have | 3 | S1 | Mohamed Triki | 🟢 Terminée | B06 |
| US-006 | Définir le contrat XSD contract-first du Warranty Service | Tâche technique | Must have | 3 | S1 | Ibrahim Ben Abdallah | 🟢 Terminée | B07 |
| US-007 | Exporter et versionner la spécification OpenAPI | Tâche technique | Must have | 2 | S1 | Yessine Koubaa, Mohamed Triki | 🟢 Terminée | B05 |
| US-037 | Regrouper les contrats dans docs/contracts (OpenAPI, .proto, XSD, WSDL) | Tâche technique | Must have | 2 | S3 | Ibrahim Ben Abdallah, Mohamed Triki, Yessine Koubaa | 🔵 À faire | B16 |

### EP-03 — Asset Service — API REST

Gestion des équipements, employés et affectations via une API REST /api/v1 documentée OpenAPI.  
Composant : Asset Service (REST) · 5 éléments · 19 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-008 | Gérer les équipements (CRUD Equipment) | User story | Must have | 5 | S1 | Yessine Koubaa | 🟢 Terminée | B04 |
| US-009 | Gérer les employés (CRUD Employee) | User story | Must have | 3 | S1 | Yessine Koubaa | 🟢 Terminée | B04 |
| US-010 | Affecter et restituer un équipement (Assignment) | User story | Must have | 5 | S1 | Yessine Koubaa | 🟢 Terminée | B04 |
| US-011 | Valider les entrées et centraliser la gestion des erreurs | User story | Must have | 3 | S1 | Yessine Koubaa | 🟢 Terminée | B05 |
| US-012 | Paginer et filtrer les listes d'équipements | User story | Could have | 3 | S3 | Yessine Koubaa | ⚪ Backlog |  |

### EP-04 — Maintenance Service — gRPC

Demandes de maintenance, planification des interventions et suivi des statuts en streaming.  
Composant : Maintenance Service (gRPC) · 4 éléments · 16 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-013 | Créer une demande de maintenance (RPC unaire) | User story | Must have | 3 | S2 | Mohamed Triki | 🟢 Terminée | B09 |
| US-014 | Planifier une intervention et gérer son cycle de vie | User story | Must have | 5 | S2 | Mohamed Triki | 🟢 Terminée | B09 |
| US-015 | Suivre les changements de statut en streaming | User story | Must have | 5 | S2 | Mohamed Triki | 🟢 Terminée | B09 |
| US-016 | Gérer les codes de statut gRPC et les deadlines | Tâche technique | Must have | 3 | S2 | Mohamed Triki | 🟢 Terminée | B09 |

### EP-05 — Warranty Service — SOAP

Vérification et consultation des garanties auprès d'un système partenaire simulé (contract-first).  
Composant : Warranty Service (SOAP) · 3 éléments · 9 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-017 | Vérifier la couverture d'une garantie (CheckWarranty) | User story | Must have | 3 | S2 | Ibrahim Ben Abdallah | 🟢 Terminée | B10 |
| US-018 | Consulter les détails d'une garantie (GetWarrantyDetails) | User story | Must have | 3 | S2 | Ibrahim Ben Abdallah | 🟢 Terminée | B10 |
| US-019 | Renvoyer des SOAP Faults métier | User story | Must have | 3 | S2 | Ibrahim Ben Abdallah | 🟢 Terminée | B10 |

### EP-06 — Communications interservices

Appels réels Asset → Warranty (SOAP) et Asset → Maintenance (gRPC) avec gestion des erreurs.  
Composant : Asset Service (REST) · 3 éléments · 15 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-020 | Appeler le Warranty Service depuis le Asset Service (SOAP) | User story | Must have | 5 | S2 | Yessine Koubaa, Ibrahim Ben Abdallah, Mohamed Triki | 🟢 Terminée | B11 |
| US-021 | Appeler le Maintenance Service depuis le Asset Service (gRPC) | User story | Must have | 5 | S2 | Yessine Koubaa, Mohamed Triki | 🟢 Terminée | B11 |
| US-022 | Déclarer une panne de bout en bout | User story | Must have | 5 | S2 | Yessine Koubaa, Mohamed Triki, Ibrahim Ben Abdallah | 🟢 Terminée | B11 |

### EP-07 — API Gateway

Point d'entrée unique, routage vers les services, CORS.  
Composant : API Gateway · 2 éléments · 6 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-023 | Initialiser Spring Cloud Gateway et les premières routes | Tâche technique | Must have | 3 | S1 | Omar Ezzeddine | 🟢 Terminée | B08 |
| US-024 | Finaliser le routage, CORS et l'exposition de Swagger via la Gateway | Tâche technique | Must have | 3 | S3 | Omar Ezzeddine, Mohamed Triki | 🟡 En cours | B12 |

### EP-08 — Qualité et tests

Tests unitaires, tests d'intégration par style de service, collection Postman / scripts.  
Composant : Infra / Docker / CI · 4 éléments · 15 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-025 | Écrire les tests unitaires de chaque service | Tâche technique | Must have | 5 | S3 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟡 En cours | B13 |
| US-026 | Écrire au moins un test d'intégration par style de service | Tâche technique | Must have | 5 | S3 | Mohamed Triki, Ibrahim Ben Abdallah, Yessine Koubaa | 🟡 En cours | B13 |
| US-027 | Fournir une collection Postman et des scripts curl / grpcurl | Tâche technique | Must have | 2 | S3 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟡 En cours | B13, B16 |
| US-038 | Tester les routes de la Gateway | Tâche technique | Must have | 3 | S3 | Omar Ezzeddine | 🔵 À faire | B13 |

### EP-09 — Conteneurisation et résilience

Docker multi-stage, Compose, configuration externalisée, supervision et résilience.  
Composant : Infra / Docker / CI · 7 éléments · 25 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-028 | Créer un Dockerfile multi-stage par service | Tâche technique | Must have | 3 | S4 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟢 Terminée | B14 |
| US-029 | Orchestrer les services avec Docker Compose | Tâche technique | Must have | 3 | S4 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟣 En revue | B14 |
| US-030 | Configuration externalisée, services sans état et Actuator | Tâche technique | Must have | 3 | S4 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟡 En cours | B15 |
| US-031 | Résilience avec Resilience4j (timeout, retry avec backoff, circuit breaker et repli) | User story | Must have | 8 | S4 | Yessine Koubaa, Mohamed Triki, Ibrahim Ben Abdallah | 🔵 À faire | B15 |
| US-032 | Ajouter un identifiant de corrélation dans les logs | Tâche technique | Could have | 2 | S4 | Yessine Koubaa | 🔵 À faire | B15 |
| US-036 | Migrer les bases H2 vers PostgreSQL (une base par service) | User story | Should have | 5 | S4 | Mohamed Triki | 🟢 Terminée | B14, B15 |
| BUG-001 | Les garanties du Warranty Service sont perdues à chaque redémarrage Docker | Anomalie | Must have | 1 | S4 | Mohamed Triki, Ibrahim Ben Abdallah | 🟢 Terminée |  |

### EP-10 — Documentation et livraison

README, rapport, démonstration et soutenance.  
Composant : Documentation / Livrables · 3 éléments · 14 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-033 | Compléter le README | Tâche technique | Must have | 3 | S5 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🟡 En cours | B16 |
| US-034 | Rédiger le rapport final (8 à 12 pages) | Tâche technique | Must have | 8 | S5 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🔵 À faire | B16 |
| US-035 | Préparer la démonstration et la soutenance | Tâche technique | Must have | 3 | S5 | Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa | 🔵 À faire | B16 |

### EP-11 — Fonctionnalités bonus

Bonus valorisés jusqu'à +2 points, à condition d'être démontrés.  
Composant : Infra / Docker / CI · 5 éléments · 26 pts

| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |
|---|---|---|---|---:|---|---|---|---|
| US-039 | Pipeline CI GitHub Actions (build + tests Maven de chaque service) | Tâche technique | Could have | 3 | S4 | Mohamed Triki | 🔵 À faire |  |
| US-040 | Sécuriser la Gateway avec JWT | User story | Could have | 5 | S5 | Omar Ezzeddine | ⚪ Backlog |  |
| US-041 | Tests d'intégration avec Testcontainers (PostgreSQL) | User story | Could have | 5 | S5 | Ibrahim Ben Abdallah, Yessine Koubaa | ⚪ Backlog |  |
| US-042 | Publier un événement « intervention terminée » via RabbitMQ | User story | Could have | 8 | S5 | Mohamed Triki, Yessine Koubaa | ⚪ Backlog |  |
| US-043 | Traçage distribué (Micrometer Tracing + OpenTelemetry) | User story | Could have | 5 | S5 | Yessine Koubaa, Omar Ezzeddine | ⚪ Backlog |  |

## Détail des éléments

<details><summary><b>US-001</b> — Initialiser le dépôt GitHub</summary>

_En tant qu'équipe, nous voulons un dépôt structuré avec un dossier par service afin de travailler en parallèle sans conflit._

**Épic :** EP-01 · **Sprint :** S0 · **Points :** 2 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C7

**Critères d’acceptation**

- [x] Un dossier par service (asset-service, maintenance-service, warranty-service, gateway)
- [x] .gitignore adapté à Maven / IDE
- [x] README initial présentant le sujet et l'équipe

**Réalisation :** branche `main` · [`2d3f4ff`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/2d3f4ff)

</details>

<details><summary><b>US-002</b> — Définir la stratégie de branches et les règles de Pull Request</summary>

_En tant qu'équipe, nous voulons une stratégie Git Flow simplifiée afin de garder main stable et d'intégrer via develop._

**Épic :** EP-01 · **Sprint :** S0 · **Points :** 2 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C7

**Critères d’acceptation**

- [x] Branches main et develop créées
- [x] Une branche feature/* par composant
- [x] Conventions de nommage des branches et des commits documentées
- [x] Intégration dans develop uniquement par Pull Request relue par au moins un membre

</details>

<details><summary><b>US-003</b> — Mettre en place le Product Backlog et le tableau GitHub Projects</summary>

_En tant qu'équipe, nous voulons un backlog versionné et synchronisé avec GitHub Issues afin de suivre l'avancement de chaque sprint._

**Épic :** EP-01 · **Sprint :** S1 · **Points :** 3 · **Responsable(s) :** Mohamed Triki · **Grille :** C7

**Critères d’acceptation**

- [ ] Backlog maintenu dans docs/backlog/backlog.json
- [ ] Issues, labels et milestones (sprints) générés automatiquement
- [ ] Tableau GitHub Projects avec les colonnes Backlog / À faire / En cours / En revue / Terminée / Bloquée
- [ ] Modèles d'Issues (story, tâche, bug) disponibles

**Réalisation :** branche `docs/product-backlog`

</details>

<details><summary><b>US-004</b> — Définir l'architecture et les flux interservices</summary>

_En tant qu'équipe, nous voulons une architecture documentée afin de partager une vision commune des services, ports et flux._

**Épic :** EP-02 · **Sprint :** S1 · **Points :** 3 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C1

**Critères d’acceptation**

- [x] Schéma d'architecture avec au moins 3 services et la Gateway
- [x] Flux Asset → Warranty (SOAP) et Asset → Maintenance (gRPC) décrits
- [x] Ports, bases de données et configuration documentés (docs/architecture-configuration.md)

</details>

<details><summary><b>US-005</b> — Définir le contrat .proto versionné du Maintenance Service</summary>

_En tant que développeur du Asset Service, je veux un contrat .proto versionné afin de générer un client gRPC typé._

**Épic :** EP-02 · **Sprint :** S1 · **Points :** 3 · **Responsable(s) :** Mohamed Triki · **Grille :** C3

**Critères d’acceptation**

- [x] Package assetpilot.maintenance.v1
- [x] Messages et RPC (création, planification, suivi en streaming) définis
- [x] Génération du code via le plugin Maven protobuf
- [x] Évolution du contrat pensée : champs réservés (reserved), pas de renumérotation, nouvelle version = nouveau package v2

**Réalisation :** branche `feature/maintenance-grpc` · [`ac60f7b`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/ac60f7b), [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-006</b> — Définir le contrat XSD contract-first du Warranty Service</summary>

_En tant que partenaire, je veux un contrat XSD défini en premier afin que le WSDL soit généré et stable._

**Épic :** EP-02 · **Sprint :** S1 · **Points :** 3 · **Responsable(s) :** Ibrahim Ben Abdallah · **Grille :** C4

**Critères d’acceptation**

- [x] warranty.xsd décrit CheckWarranty et GetWarrantyDetails (requêtes / réponses)
- [x] WSDL généré et exposé par Spring WS
- [x] Types de fautes définis dans le contrat
- [x] WSDL généré exporté dans le dépôt (docs/contracts) et validé

</details>

<details><summary><b>US-007</b> — Exporter et versionner la spécification OpenAPI</summary>

_En tant que consommateur de l'API, je veux une spécification OpenAPI versionnée afin de connaître le contrat REST sans lancer le service._

**Épic :** EP-02 · **Sprint :** S1 · **Points :** 2 · **Responsable(s) :** Yessine Koubaa, Mohamed Triki · **Grille :** C2

**Critères d’acceptation**

- [x] Swagger UI accessible via la Gateway
- [x] Spécification exportée dans docs/openapi
- [x] Descriptions, exemples et codes d'erreur renseignés
- [x] Spécification à jour avec le code (régénérée à chaque changement d'API)

**Réalisation :** branche `feature/services-integration` · [`07bf25d`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/07bf25d)

</details>

<details><summary><b>US-008</b> — Gérer les équipements (CRUD Equipment)</summary>

_En tant que gestionnaire du parc, je veux créer, consulter, modifier et supprimer des équipements afin de tenir l'inventaire à jour._

**Épic :** EP-03 · **Sprint :** S1 · **Points :** 5 · **Responsable(s) :** Yessine Koubaa · **Grille :** C2

**Critères d’acceptation**

- [x] Endpoints GET/POST/PUT/DELETE sous /api/v1/equipments
- [x] Numéro de série unique, statut (AVAILABLE, ASSIGNED, IN_MAINTENANCE, RETIRED)
- [x] Codes HTTP corrects (201, 204, 404, 409)

</details>

<details><summary><b>US-009</b> — Gérer les employés (CRUD Employee)</summary>

_En tant que gestionnaire du parc, je veux gérer les employés afin de pouvoir leur affecter des équipements._

**Épic :** EP-03 · **Sprint :** S1 · **Points :** 3 · **Responsable(s) :** Yessine Koubaa · **Grille :** C2

**Critères d’acceptation**

- [x] Endpoints CRUD sous /api/v1/employees
- [x] Email unique et validé
- [x] Suppression refusée si l'employé a une affectation active

</details>

<details><summary><b>US-010</b> — Affecter et restituer un équipement (Assignment)</summary>

_En tant que gestionnaire du parc, je veux affecter un équipement à un employé puis enregistrer sa restitution afin de suivre qui détient quoi._

**Épic :** EP-03 · **Sprint :** S1 · **Points :** 5 · **Responsable(s) :** Yessine Koubaa · **Grille :** C2

**Critères d’acceptation**

- [x] Création d'une affectation uniquement si l'équipement est AVAILABLE
- [x] Restitution qui clôture l'affectation et remet l'équipement à AVAILABLE
- [x] Historique des affectations consultable par équipement et par employé

</details>

<details><summary><b>US-011</b> — Valider les entrées et centraliser la gestion des erreurs</summary>

_En tant que client de l'API, je veux des erreurs homogènes et explicites afin de corriger facilement mes requêtes._

**Épic :** EP-03 · **Sprint :** S1 · **Points :** 3 · **Responsable(s) :** Yessine Koubaa · **Grille :** C2

**Critères d’acceptation**

- [x] Bean Validation sur les DTO
- [x] @RestControllerAdvice renvoyant un format d'erreur unique (ProblemDetail)
- [x] Erreurs 400 / 404 / 409 / 503 documentées dans OpenAPI

</details>

<details><summary><b>US-012</b> — Paginer et filtrer les listes d'équipements</summary>

_En tant que gestionnaire du parc, je veux paginer et filtrer les équipements par statut ou type afin de retrouver rapidement un matériel._

**Épic :** EP-03 · **Sprint :** S3 · **Points :** 3 · **Responsable(s) :** Yessine Koubaa · **Grille :** C2

**Critères d’acceptation**

- [ ] Paramètres page, size, sort
- [ ] Filtres status et type
- [ ] Réponse paginée documentée dans OpenAPI

</details>

<details><summary><b>US-013</b> — Créer une demande de maintenance (RPC unaire)</summary>

_En tant que Asset Service, je veux créer une demande de maintenance via gRPC afin d'enregistrer une panne déclarée._

**Épic :** EP-04 · **Sprint :** S2 · **Points :** 3 · **Responsable(s) :** Mohamed Triki · **Grille :** C3

**Critères d’acceptation**

- [x] RPC CreateMaintenanceRequest
- [x] Validation des champs → INVALID_ARGUMENT
- [x] Demande persistée dans la base propre au service

**Réalisation :** branche `feature/maintenance-grpc` · [`ac60f7b`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/ac60f7b), [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-014</b> — Planifier une intervention et gérer son cycle de vie</summary>

_En tant que technicien, je veux planifier une intervention et faire évoluer son statut afin de suivre la réparation._

**Épic :** EP-04 · **Sprint :** S2 · **Points :** 5 · **Responsable(s) :** Mohamed Triki · **Grille :** C3

**Critères d’acceptation**

- [x] RPC ScheduleIntervention et mise à jour de statut
- [x] Transitions autorisées contrôlées (ex. COMPLETED non modifiable) → FAILED_PRECONDITION
- [x] Ressource inconnue → NOT_FOUND

**Réalisation :** branche `feature/maintenance-grpc` · [`ac60f7b`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/ac60f7b), [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-015</b> — Suivre les changements de statut en streaming</summary>

_En tant que client, je veux m'abonner aux changements de statut d'une intervention afin d'être notifié en temps réel._

**Épic :** EP-04 · **Sprint :** S2 · **Points :** 5 · **Responsable(s) :** Mohamed Triki · **Grille :** C3

**Critères d’acceptation**

- [x] RPC server-streaming de suivi
- [x] Chaque changement de statut est poussé aux abonnés
- [x] Le flux se termine quand l'intervention est COMPLETED ou CANCELLED

**Réalisation :** branche `feature/maintenance-grpc` · [`ac60f7b`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/ac60f7b), [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-016</b> — Gérer les codes de statut gRPC et les deadlines</summary>

_En tant que service appelant, je veux des codes gRPC explicites et des deadlines afin de ne jamais rester bloqué._

**Épic :** EP-04 · **Sprint :** S2 · **Points :** 3 · **Responsable(s) :** Mohamed Triki · **Grille :** C3

**Critères d’acceptation**

- [x] Exceptions métier traduites en Status gRPC
- [x] Deadline configurée côté client (3 s)
- [x] DEADLINE_EXCEEDED / UNAVAILABLE gérés côté appelant
- [x] Deadline aussi sur le RPC en streaming

**Réalisation :** branche `feature/maintenance-grpc` · [`ac60f7b`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/ac60f7b), [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-017</b> — Vérifier la couverture d'une garantie (CheckWarranty)</summary>

_En tant que Asset Service, je veux savoir si un équipement est couvert afin de décider de la prise en charge d'une intervention._

**Épic :** EP-05 · **Sprint :** S2 · **Points :** 3 · **Responsable(s) :** Ibrahim Ben Abdallah · **Grille :** C4

**Critères d’acceptation**

- [x] Opération SOAP CheckWarranty conforme au XSD
- [x] Réponse : couvert / non couvert, date de fin
- [x] Testée via SoapUI / Postman

</details>

<details><summary><b>US-018</b> — Consulter les détails d'une garantie (GetWarrantyDetails)</summary>

_En tant que gestionnaire du parc, je veux consulter les détails d'une garantie afin de connaître le fournisseur, le type et la période de couverture._

**Épic :** EP-05 · **Sprint :** S2 · **Points :** 3 · **Responsable(s) :** Ibrahim Ben Abdallah · **Grille :** C4

**Critères d’acceptation**

- [x] Opération SOAP GetWarrantyDetails conforme au XSD
- [x] Données de garanties simulées (partenaire externe)

</details>

<details><summary><b>US-019</b> — Renvoyer des SOAP Faults métier</summary>

_En tant que client SOAP, je veux des SOAP Faults explicites afin de distinguer une garantie inexistante d'une requête invalide._

**Épic :** EP-05 · **Sprint :** S2 · **Points :** 3 · **Responsable(s) :** Ibrahim Ben Abdallah · **Grille :** C4

**Critères d’acceptation**

- [x] Fault WARRANTY_NOT_FOUND
- [x] Fault pour requête invalide
- [x] Détail de la faute structuré et documenté

</details>

<details><summary><b>US-020</b> — Appeler le Warranty Service depuis le Asset Service (SOAP)</summary>

_En tant que Asset Service, je veux vérifier la garantie via SOAP avant une intervention afin d'indiquer si elle est couverte._

**Épic :** EP-06 · **Sprint :** S2 · **Points :** 5 · **Responsable(s) :** Yessine Koubaa, Ibrahim Ben Abdallah, Mohamed Triki · **Grille :** C1, C4

**Critères d’acceptation**

- [x] Client Spring WS généré depuis le XSD
- [x] WARRANTY_NOT_FOUND → covered=false, status=NOT_FOUND
- [x] Service injoignable / timeout → repli status=UNKNOWN, verified=false

**Dépend de :** US-017, US-019

**Réalisation :** branche `feature/services-integration` · [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-021</b> — Appeler le Maintenance Service depuis le Asset Service (gRPC)</summary>

_En tant que Asset Service, je veux créer et planifier une intervention via gRPC afin de déclencher la maintenance d'un équipement._

**Épic :** EP-06 · **Sprint :** S2 · **Points :** 5 · **Responsable(s) :** Yessine Koubaa, Mohamed Triki · **Grille :** C1, C3

**Critères d’acceptation**

- [x] Client gRPC généré depuis le .proto
- [x] Deadline de 3 s par appel
- [x] Erreurs gRPC traduites en réponses HTTP cohérentes

**Dépend de :** US-013, US-014, US-016

**Réalisation :** branche `feature/services-integration` · [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-022</b> — Déclarer une panne de bout en bout</summary>

_En tant que gestionnaire du parc, je veux déclarer une panne sur un équipement afin que la garantie soit vérifiée et l'intervention créée automatiquement._

**Épic :** EP-06 · **Sprint :** S2 · **Points :** 5 · **Responsable(s) :** Yessine Koubaa, Mohamed Triki, Ibrahim Ben Abdallah · **Grille :** C1

**Critères d’acceptation**

- [x] POST /api/v1/equipments/{id}/maintenance-requests
- [x] Vérification garantie puis création / planification de l'intervention
- [x] Équipement en IN_MAINTENANCE, puis ASSIGNED/AVAILABLE à la clôture

**Dépend de :** US-020, US-021

**Réalisation :** branche `feature/services-integration` · [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-023</b> — Initialiser Spring Cloud Gateway et les premières routes</summary>

_En tant que client, je veux un point d'entrée unique afin de ne pas connaître l'adresse de chaque service._

**Épic :** EP-07 · **Sprint :** S1 · **Points :** 3 · **Responsable(s) :** Omar Ezzeddine · **Grille :** C1

**Critères d’acceptation**

- [x] Projet Gateway Spring Boot 3.3.x
- [x] Route /api/v1/** → Asset Service
- [x] Route /ws/** → Warranty Service

</details>

<details><summary><b>US-024</b> — Finaliser le routage, CORS et l'exposition de Swagger via la Gateway</summary>

_En tant que front-end, je veux accéder à toutes les API et à leur documentation via la Gateway afin de simplifier l'intégration._

**Épic :** EP-07 · **Sprint :** S3 · **Points :** 3 · **Responsable(s) :** Omar Ezzeddine, Mohamed Triki · **Grille :** C1

**Critères d’acceptation**

- [ ] Routes /swagger-ui/** et /v3/api-docs/** exposées
- [ ] CORS configuré pour le front-end
- [ ] URLs des services externalisées (variables d'environnement)
- [ ] Tests de routage

**Dépend de :** US-023

**Réalisation :** [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5), [`07bf25d`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/07bf25d)

</details>

<details><summary><b>US-025</b> — Écrire les tests unitaires de chaque service</summary>

_En tant qu'équipe, nous voulons des tests unitaires sur la logique métier afin d'éviter les régressions._

**Épic :** EP-08 · **Sprint :** S3 · **Points :** 5 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C7

**Critères d’acceptation**

- [ ] Tests des services / règles métier de chaque microservice
- [ ] mvn test passe pour chaque module

</details>

<details><summary><b>US-026</b> — Écrire au moins un test d'intégration par style de service</summary>

_En tant qu'équipe, nous voulons valider chaque protocole de bout en bout afin de garantir le respect des contrats._

**Épic :** EP-08 · **Sprint :** S3 · **Points :** 5 · **Responsable(s) :** Mohamed Triki, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C7

**Critères d’acceptation**

- [ ] Test d'intégration REST (MockMvc / WebTestClient)
- [ ] Test d'intégration gRPC (serveur in-process)
- [ ] Test d'intégration SOAP (MockWebServiceClient)
- [ ] Test du flux interservices

**Réalisation :** [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-027</b> — Fournir une collection Postman et des scripts curl / grpcurl</summary>

_En tant qu'évaluateur, je veux une collection de tests prête à l'emploi afin de rejouer les scénarios rapidement._

**Épic :** EP-08 · **Sprint :** S3 · **Points :** 2 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C8

**Critères d’acceptation**

- [ ] Collection Postman couvrant REST, SOAP et le flux principal
- [ ] Exemples grpcurl pour le Maintenance Service

**Réalisation :** [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-028</b> — Créer un Dockerfile multi-stage par service</summary>

_En tant qu'équipe, nous voulons des images légères et reproductibles afin de déployer chaque service de façon identique._

**Épic :** EP-09 · **Sprint :** S4 · **Points :** 3 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C5

**Critères d’acceptation**

- [x] Étape build Maven + étape runtime JRE 17 (2 FROM)
- [x] Utilisateur non-root
- [x] Image construite sans dépendance locale

**Réalisation :** branche `feature/services-integration` · [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5)

</details>

<details><summary><b>US-029</b> — Orchestrer les services avec Docker Compose</summary>

_En tant qu'évaluateur, je veux lancer toute la plateforme en une commande afin de tester facilement._

**Épic :** EP-09 · **Sprint :** S4 · **Points :** 3 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C5

**Critères d’acceptation**

- [ ] docker-compose.yml à la racine
- [ ] Réseau interne et volumes
- [ ] Healthchecks et depends_on: condition: service_healthy
- [ ] Lancement validé par « docker compose up » sur une machine neuve, sans manipulation manuelle
- [ ] Découverte des services par le DNS de Docker Compose (noms de services, pas d'IP)

**Dépend de :** US-028

**Réalisation :** branche `feature/database-postgresql` · [`becc8d5`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/becc8d5), [`422407c`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/422407c)

</details>

<details><summary><b>US-030</b> — Configuration externalisée, services sans état et Actuator</summary>

_En tant qu'opérateur, je veux configurer les services par variables d'environnement et surveiller leur santé afin d'exploiter la plateforme._

**Épic :** EP-09 · **Sprint :** S4 · **Points :** 3 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C6

**Critères d’acceptation**

- [ ] Ports, URLs, identifiants et timeouts injectés par variables d'environnement ou fichier .env
- [ ] Fichier .env.example versionné, .env ignoré par git, aucun secret en dur dans le dépôt
- [ ] Services sans état (aucune session en mémoire, état uniquement en base)
- [ ] Logs sur la sortie standard (pas de fichier de log)
- [ ] /actuator/health exposé sur chaque service et utilisé par les healthchecks

**Réalisation :** branche `feature/database-postgresql` · [`422407c`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/422407c)

</details>

<details><summary><b>US-031</b> — Résilience avec Resilience4j (timeout, retry avec backoff, circuit breaker et repli)</summary>

_En tant qu'utilisateur, je veux que la plateforme reste utilisable quand un service tombe afin de ne pas bloquer la déclaration de pannes._

**Épic :** EP-09 · **Sprint :** S4 · **Points :** 8 · **Responsable(s) :** Yessine Koubaa, Mohamed Triki, Ibrahim Ben Abdallah · **Grille :** C6

**Critères d’acceptation**

- [ ] Dépendance resilience4j-spring-boot3 dans le Asset Service
- [ ] Timeout explicite sur tous les appels réseau (SOAP via WebServiceTemplate, gRPC via deadline, Gateway)
- [ ] @Retry avec backoff exponentiel et @CircuitBreaker sur les appels Warranty (SOAP) et Maintenance (gRPC)
- [ ] Méthode de repli (fallbackMethod) qui renvoie une réponse dégradée cohérente
- [ ] État du circuit breaker visible via /actuator/health ou /actuator/circuitbreakers
- [ ] Panne simulée démontrée : docker compose stop warranty-service → la déclaration de panne fonctionne en mode dégradé

**Dépend de :** US-020, US-021

</details>

<details><summary><b>US-032</b> — Ajouter un identifiant de corrélation dans les logs</summary>

_En tant que développeur, je veux suivre une requête à travers les services afin de diagnostiquer plus vite._

**Épic :** EP-09 · **Sprint :** S4 · **Points :** 2 · **Responsable(s) :** Yessine Koubaa · **Grille :** C6

**Critères d’acceptation**

- [ ] En-tête de corrélation propagé par la Gateway et les clients SOAP / gRPC
- [ ] Identifiant présent dans les logs (MDC)

</details>

<details><summary><b>US-033</b> — Compléter le README</summary>

_En tant qu'évaluateur, je veux un README complet afin de comprendre, lancer et tester le projet._

**Épic :** EP-10 · **Sprint :** S5 · **Points :** 3 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C8

**Critères d’acceptation**

- [ ] Sujet, architecture, prérequis
- [ ] Commandes de lancement et de test
- [ ] Ports, URLs et exemples d'appels
- [ ] Liens vers les contrats (OpenAPI, .proto, XSD/WSDL)

</details>

<details><summary><b>US-034</b> — Rédiger le rapport final (8 à 12 pages)</summary>

_En tant qu'équipe, nous voulons un rapport PDF clair afin de justifier nos choix d'architecture et présenter le travail._

**Épic :** EP-10 · **Sprint :** S5 · **Points :** 8 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C8

**Critères d’acceptation**

- [ ] Problème traité et découpage en services
- [ ] Choix d'architecture et justification de chaque protocole (REST, gRPC, SOAP)
- [ ] Difficultés rencontrées, limites et pistes d'amélioration
- [ ] 8 à 12 pages, export PDF

</details>

<details><summary><b>US-035</b> — Préparer la démonstration et la soutenance</summary>

_En tant qu'équipe, nous voulons un scénario de démonstration répété afin de tenir les 15 minutes et répondre aux questions individuelles._

**Épic :** EP-10 · **Sprint :** S5 · **Points :** 3 · **Responsable(s) :** Mohamed Triki, Omar Ezzeddine, Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** C8

**Critères d’acceptation**

- [ ] Démonstration de 15 minutes couvrant REST, gRPC (unaire + streaming), SOAP (dont un SOAP Fault)
- [ ] Démarrage via « docker compose up » et panne simulée en direct (résilience)
- [ ] Chaque membre capable d'expliquer l'ensemble du projet (questions individuelles sur papier)

</details>

<details><summary><b>US-036</b> — Migrer les bases H2 vers PostgreSQL (une base par service)</summary>

_En tant qu'équipe, nous voulons une base PostgreSQL par service afin de persister les données de façon fiable (l'énoncé accepte aussi un stockage propre : H2 fichier + volume suffit, PostgreSQL est un plus)._

**Épic :** EP-09 · **Sprint :** S4 · **Points :** 5 · **Responsable(s) :** Mohamed Triki · **Grille :** C5, C6

**Critères d’acceptation**

- [x] Driver org.postgresql:postgresql dans asset, maintenance et warranty (H2 conservé pour les tests)
- [x] Un conteneur PostgreSQL par service (assetdb, maintenancedb, warrantydb) avec volume et healthcheck pg_isready
- [x] URL, utilisateur et mot de passe externalisés par variables d'environnement
- [x] depends_on: condition: service_healthy entre chaque service et sa base

**Réalisation :** branche `feature/database-postgresql` · [`422407c`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/422407c), [`2ceb2fe`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/2ceb2fe)

</details>

<details><summary><b>BUG-001</b> — Les garanties du Warranty Service sont perdues à chaque redémarrage Docker</summary>

_Dans docker-compose, warranty-service utilise H2 en mémoire avec ddl-auto=create-drop et sans volume._

**Épic :** EP-09 · **Sprint :** S4 · **Points :** 1 · **Responsable(s) :** Mohamed Triki, Ibrahim Ben Abdallah · **Grille :** C5

**Critères d’acceptation**

- [x] Données de garanties persistées entre deux redémarrages (volume ou PostgreSQL)

**Réalisation :** branche `feature/database-postgresql` · [`422407c`](https://github.com/MohamedTrikii/AssetPilot_SOA/commit/422407c)

</details>

<details><summary><b>US-037</b> — Regrouper les contrats dans docs/contracts (OpenAPI, .proto, XSD, WSDL)</summary>

_En tant qu'évaluatrice, je veux trouver tous les contrats au même endroit afin de les vérifier sans lancer les services._

**Épic :** EP-02 · **Sprint :** S3 · **Points :** 2 · **Responsable(s) :** Ibrahim Ben Abdallah, Mohamed Triki, Yessine Koubaa · **Grille :** C1, C8

**Critères d’acceptation**

- [ ] openapi.yaml, maintenance.proto, warranty.xsd et warranty.wsdl présents dans docs/contracts
- [ ] Copies embarquées dans asset-service synchronisées avec les originaux
- [ ] Liens depuis le README

</details>

<details><summary><b>US-038</b> — Tester les routes de la Gateway</summary>

_En tant qu'équipe, nous voulons vérifier le routage de la Gateway afin de garantir le point d'entrée unique._

**Épic :** EP-08 · **Sprint :** S3 · **Points :** 3 · **Responsable(s) :** Omar Ezzeddine · **Grille :** C7

**Critères d’acceptation**

- [ ] Test des routes /api/v1/**, /ws/**, /swagger-ui/**
- [ ] Test de la configuration CORS

</details>

<details><summary><b>US-039</b> — Pipeline CI GitHub Actions (build + tests Maven de chaque service)</summary>

_En tant qu'équipe, nous voulons une CI qui construit et teste chaque service à chaque PR afin de détecter les régressions avant la fusion._

**Épic :** EP-11 · **Sprint :** S4 · **Points :** 3 · **Responsable(s) :** Mohamed Triki · **Grille :** BONUS, C7

**Critères d’acceptation**

- [ ] Workflow par service (matrix) : mvn -B verify
- [ ] Build des images Docker
- [ ] Badge de statut dans le README

</details>

<details><summary><b>US-040</b> — Sécuriser la Gateway avec JWT</summary>

_En tant qu'administrateur, je veux que seules les requêtes authentifiées passent la Gateway afin de protéger les API._

**Épic :** EP-11 · **Sprint :** S5 · **Points :** 5 · **Responsable(s) :** Omar Ezzeddine · **Grille :** BONUS

**Critères d’acceptation**

- [ ] Validation d'un JWT (Spring Security resource server)
- [ ] Clé / issuer injectés par variable d'environnement
- [ ] Démontré avec Postman (401 sans jeton, 200 avec)

</details>

<details><summary><b>US-041</b> — Tests d'intégration avec Testcontainers (PostgreSQL)</summary>

_En tant qu'équipe, nous voulons tester la persistance contre une vraie base PostgreSQL afin d'éviter les écarts H2 / production._

**Épic :** EP-11 · **Sprint :** S5 · **Points :** 5 · **Responsable(s) :** Ibrahim Ben Abdallah, Yessine Koubaa · **Grille :** BONUS, C7

**Critères d’acceptation**

- [ ] @Testcontainers + PostgreSQLContainer sur au moins un service
- [ ] Exécuté par la CI

**Dépend de :** US-036

</details>

<details><summary><b>US-042</b> — Publier un événement « intervention terminée » via RabbitMQ</summary>

_En tant que Asset Service, je veux être notifié de façon asynchrone de la fin d'une intervention afin de mettre à jour le statut de l'équipement sans appel synchrone._

**Épic :** EP-11 · **Sprint :** S5 · **Points :** 8 · **Responsable(s) :** Mohamed Triki, Yessine Koubaa · **Grille :** BONUS

**Critères d’acceptation**

- [ ] Conteneur RabbitMQ dans docker-compose
- [ ] Événement publié par Maintenance et consommé par Asset
- [ ] Démontré en soutenance

</details>

<details><summary><b>US-043</b> — Traçage distribué (Micrometer Tracing + OpenTelemetry)</summary>

_En tant que développeur, je veux visualiser une requête à travers la Gateway et les services afin de diagnostiquer les lenteurs._

**Épic :** EP-11 · **Sprint :** S5 · **Points :** 5 · **Responsable(s) :** Yessine Koubaa, Omar Ezzeddine · **Grille :** BONUS, C6

**Critères d’acceptation**

- [ ] Micrometer Tracing + exporteur OTLP
- [ ] Conteneur Jaeger / Zipkin dans docker-compose
- [ ] Trace complète Gateway → Asset → Warranty / Maintenance démontrée

**Dépend de :** US-032

</details>

## Définition de « Terminé » (DoD)

- Code relu et intégré dans `develop` via Pull Request approuvée par au moins un autre membre.
- Tests du composant passants (`mvn test`) et critères d’acceptation satisfaits.
- Contrats (OpenAPI, .proto, XSD/WSDL) et documentation à jour.
