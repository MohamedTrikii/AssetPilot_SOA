# AssetPilot — Maintenance Service (gRPC)

Microservice de la plateforme **AssetPilot** chargé de la gestion de la maintenance du parc informatique :

1. les demandes de maintenance des équipements ;
2. la planification des interventions ;
3. le suivi des statuts des interventions.

La communication se fait exclusivement via **gRPC / Protocol Buffers**. Le principal client est l'Asset Service, qui crée et planifie des interventions après avoir vérifié la garantie auprès du Warranty Service (SOAP).

Responsable : Mohamed Triki

## Stack technique

| Élément | Version |
|---|---|
| Java (cible de compilation) | 17 |
| Spring Boot | 3.3.13 |
| Maven | 3.9+ |
| gRPC Java | 1.65.1 |
| Protocol Buffers | 3.25.8 |
| grpc-server-spring-boot-starter (net.devh) | 3.1.0.RELEASE |

## Structure

```
maintenance-service/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/assetpilot/maintenance/
    │   │   └── MaintenanceApplication.java
    │   ├── proto/assetpilot/maintenance/v1/
    │   │   └── maintenance.proto          # contrat gRPC versionné
    │   └── resources/
    │       └── application.yml
    └── test/java/com/assetpilot/maintenance/
        └── MaintenanceApplicationTests.java
```

Packages prévus pour les sprints suivants :

| Package | Rôle |
|---|---|
| `com.assetpilot.maintenance.grpc` | Couche de communication : implémentations des services gRPC, conversion messages proto ↔ modèle métier |
| `com.assetpilot.maintenance.service` | Logique métier (création, planification, transitions de statut) |
| `com.assetpilot.maintenance.model` | Objets métier, indépendants de protobuf |
| `com.assetpilot.maintenance.repository` | Persistance |
| `com.assetpilot.maintenance.config` | Configuration Spring / gRPC |
| `com.assetpilot.maintenance.proto.v1` | **Classes générées** depuis le `.proto` (dans `target/`, ne jamais modifier) |

## Contrat gRPC

Fichier : [`src/main/proto/assetpilot/maintenance/v1/maintenance.proto`](src/main/proto/assetpilot/maintenance/v1/maintenance.proto)
Package : `assetpilot.maintenance.v1`

| RPC | Type | Description |
|---|---|---|
| `CreateMaintenanceRequest` | Unaire | Crée une demande de maintenance pour un équipement |
| `ScheduleIntervention` | Unaire | Planifie une intervention pour une demande existante |
| `TrackInterventionStatus` | Server streaming | Flux des changements de statut d'une intervention (statut courant puis chaque changement ; fin à COMPLETED/CANCELLED) |
| `GetIntervention` | Unaire | Consulte une intervention |
| `UpdateInterventionStatus` | Unaire | Fait évoluer le statut (transitions contrôlées, diffusées au flux) |

Erreurs : `INVALID_ARGUMENT` (champ manquant, date passée), `NOT_FOUND` (demande/intervention inconnue), `FAILED_PRECONDITION` (transition interdite, demande non ouverte). Les clients doivent fixer un deadline (`withDeadlineAfter`).

Ports : gRPC `9090`, HTTP d'administration `8084` (`/actuator/health`). Base H2 propre (`maintenancedb`).

Statuts :

- `MaintenanceRequestStatus` : `OPEN` → `SCHEDULED` → `RESOLVED`, ou `CANCELLED`
- `InterventionStatus` : `SCHEDULED` → `IN_PROGRESS` → `COMPLETED`, ou `CANCELLED`

Règles d'évolution du contrat : ne jamais réutiliser un numéro de champ, déclarer `reserved` les champs supprimés, créer un package `v2` en cas de rupture de compatibilité.

## Génération du code

La génération est automatique à chaque build Maven (`protobuf-maven-plugin`) :

- `protobuf:compile` → messages et énumérations dans `target/generated-sources/protobuf/java`
- `protobuf:compile-custom` → stubs gRPC (`MaintenanceServiceGrpc`) dans `target/generated-sources/protobuf/grpc-java`

Le binaire `protoc` adapté à l'OS est téléchargé automatiquement (`os-maven-plugin`).

## Build

```bash
mvn clean verify
```

`JAVA_HOME` doit pointer vers un JDK 17 (ou plus récent, jusqu'à 21) ; le code est compilé avec `--release 17`.

## Configuration

`src/main/resources/application.yml` :

| Propriété | Valeur | Description |
|---|---|---|
| `spring.application.name` | `maintenance-service` | Nom du service |
| `grpc.server.port` | `9090` | Port du serveur gRPC |
