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

- **Asset Service** : gestion des équipements, employés et affectations.
- **Maintenance Service** : demandes de maintenance, planification et suivi des interventions.
- **Warranty Service** : vérification et consultation des garanties.
- **API Gateway** : point d'entrée et routage vers les services.

Flux entre services :
- Asset Service → Maintenance Service (gRPC)
- Asset Service → Warranty Service (SOAP)

Chaque service possède sa propre base de données.

## Technologies

Java 17, Spring Boot 3.3.x, Maven, Spring Cloud Gateway, REST/OpenAPI, gRPC, Spring Web Services, Docker Compose.

## Branches

- `main` : version stable
- `develop` : intégration
- `feature/*` : une branche par service
