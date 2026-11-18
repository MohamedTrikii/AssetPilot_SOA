# AssetPilot — Warranty Service (SOAP)

Microservice de la plateforme **AssetPilot** chargé de la vérification et de la consultation des garanties d'équipements informatiques auprès d'un système partenaire simulé.

La communication s'effectue exclusivement via **SOAP / XML (Spring Web Services)** avec une approche **Contract-First** basée sur un contrat XSD strict. L'Asset Service est le principal client consommateur de ce service SOAP avant d'engager des demandes ou interventions de maintenance.

**Responsable :** Ibrahim Ben Abdallah

---

## Stack technique

| Élément | Version |
|---|---|
| Java (cible de compilation) | 17 |
| Spring Boot | 3.3.13 |
| Maven | 3.9+ |
| Spring Web Services | 4.0.15 (Spring Boot Starter Web Services) |
| WSDL4J | 1.6.3 |
| Jakarta XML Binding (JAXB) | 4.0 (Jakarta EE 10) |
| jaxb-maven-plugin (org.jvnet.jaxb) | 4.0.8 |
| Base de données | H2 (in-memory, autonome par microservice) / JPA |

---

## Structure du projet

```
warranty-service/
├── .gitignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/assetpilot/warranty/
    │   │   ├── WarrantyApplication.java                         # Point d'entrée Spring Boot & seeding données
    │   │   ├── config/
    │   │   │   └── WebServiceConfig.java                        # Configuration Spring-WS, servlet /ws/* & WSDL
    │   │   ├── endpoint/
    │   │   │   └── WarrantyEndpoint.java                        # Endpoint SOAP (@PayloadRoot)
    │   │   ├── exception/
    │   │   │   ├── DetailSoapFaultDefinitionExceptionResolver.java # Résolution et enrichissement des SOAP Faults
    │   │   │   ├── InvalidContractException.java                # Fault Client en cas de requête invalide
    │   │   │   └── WarrantyNotFoundException.java               # Fault Client si garantie inexistante
    │   │   ├── model/
    │   │   │   └── WarrantyEntity.java                          # Entité JPA persistée en base autonome
    │   │   ├── repository/
    │   │   │   └── WarrantyRepository.java                      # Repository Spring Data JPA
    │   │   └── service/
    │   │       ├── WarrantyService.java                         # Interface métier
    │   │       └── impl/WarrantyServiceImpl.java                # Implémentation de la logique métier
    │   └── resources/
    │       ├── application.yml                                  # Configuration du service (port 8082, H2, logging)
    │       └── xsd/
    │           └── warranty.xsd                                 # Contrat XSD Contract-First
    └── test/java/com/assetpilot/warranty/
        ├── WarrantyApplicationTests.java                       # Test de chargement du contexte
        └── endpoint/
            ├── WarrantyEndpointTest.java                        # Tests unitaires du WarrantyEndpoint
            └── WarrantySoapIntegrationTest.java                 # Tests d'intégration SOAP complets (MockWebServiceClient)
```

### Rôle des packages

| Package | Rôle |
|---|---|
| `com.assetpilot.warranty.config` | Enregistrement de `MessageDispatcherServlet`, exposition dynamique du WSDL et configuration SOAP |
| `com.assetpilot.warranty.endpoint` | Contrôleurs SOAP (@Endpoint), réception et routage des requêtes XML `@PayloadRoot` |
| `com.assetpilot.warranty.service` | Logique métier de vérification d'éligibilité et de gestion des garanties |
| `com.assetpilot.warranty.model` | Entités JPA métier stockées dans la base de données propre au service |
| `com.assetpilot.warranty.repository` | Persistance et requêtage JPA |
| `com.assetpilot.warranty.exception` | Exceptions métier et mapping en **SOAP Faults** conformes |
| `com.assetpilot.warranty.soap.v1` | **Classes Java générées automatiquement** depuis le XSD dans `target/generated-sources/jaxb` |

---

## Contrat XSD (Contract-First)

Fichier : [`src/main/resources/xsd/warranty.xsd`](src/main/resources/xsd/warranty.xsd)  
Target Namespace : `http://assetpilot.com/warranty/v1`

### Opérations exposées

| Opération SOAP | Élément Requête | Élément Réponse | Description |
|---|---|---|---|
| `CheckWarranty` | `CheckWarrantyRequest` | `CheckWarrantyResponse` | Vérifie si un équipement (`assetId`) dispose d'une garantie active, sa date d'expiration et son type de couverture |
| `GetWarrantyDetails` | `GetWarrantyDetailsRequest` | `GetWarrantyDetailsResponse` | Récupère le contrat complet (fournisseur, dates début/fin, SLA, termes de couverture, contact support) |

### Énumérations du domaine

- **WarrantyStatus** : `ACTIVE`, `EXPIRED`, `VOIDED`, `PENDING`
- **CoverageType** : `STANDARD`, `PREMIUM`, `EXTENDED`, `PARTS_ONLY`, `LABOR_ONLY`

---

## Publication WSDL

Le contrat WSDL 1.1 est généré et publié dynamiquement au démarrage par Spring-WS via `DefaultWsdl11Definition` :

```
http://localhost:8082/ws/warranties.wsdl
```

---

## Exemples d'appels SOAP

### 1. Vérification de garantie (`CheckWarranty`)

**Requête SOAP :**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:v1="http://assetpilot.com/warranty/v1">
   <soapenv:Header/>
   <soapenv:Body>
      <v1:CheckWarrantyRequest>
         <v1:assetId>AST-001</v1:assetId>
      </v1:CheckWarrantyRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Réponse SOAP :**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:CheckWarrantyResponse xmlns:ns2="http://assetpilot.com/warranty/v1">
         <ns2:assetId>AST-001</ns2:assetId>
         <ns2:isCovered>true</ns2:isCovered>
         <ns2:status>ACTIVE</ns2:status>
         <ns2:expiryDate>2028-11-15</ns2:expiryDate>
         <ns2:coverageType>PREMIUM</ns2:coverageType>
         <ns2:message>Équipement sous garantie valide jusqu'au 2028-11-15 (Dell Technologies Partner Care - 24/7 4h On-site Support)</ns2:message>
      </ns2:CheckWarrantyResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

### 2. Consultation des détails du contrat (`GetWarrantyDetails`)

**Requête SOAP :**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:v1="http://assetpilot.com/warranty/v1">
   <soapenv:Header/>
   <soapenv:Body>
      <v1:GetWarrantyDetailsRequest>
         <v1:assetId>AST-001</v1:assetId>
      </v1:GetWarrantyDetailsRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Réponse SOAP :**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:GetWarrantyDetailsResponse xmlns:ns2="http://assetpilot.com/warranty/v1">
         <ns2:warranty>
            <ns2:id>WAR-1001</ns2:id>
            <ns2:assetId>AST-001</ns2:assetId>
            <ns2:contractNumber>DELL-PRO-2024-001</ns2:contractNumber>
            <ns2:provider>Dell Technologies Partner Care</ns2:provider>
            <ns2:startDate>2024-05-15</ns2:startDate>
            <ns2:endDate>2028-11-15</ns2:endDate>
            <ns2:status>ACTIVE</ns2:status>
            <ns2:coverageType>PREMIUM</ns2:coverageType>
            <ns2:slaLevel>24/7 4h On-site Support</ns2:slaLevel>
            <ns2:terms>Couverture pièces, main d'œuvre et intervention sur site sous 4 heures.</ns2:terms>
            <ns2:supportContact>support-dell@partner.assetpilot.com</ns2:supportContact>
         </ns2:warranty>
      </ns2:GetWarrantyDetailsResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

### 3. Gestion des erreurs (SOAP Fault)

Lorsqu'un équipement n'est pas trouvé ou que la requête est invalide, un **SOAP Fault** standardisé (code `SOAP-ENV:Client`) enrichi d'un élément `<ServiceFault>` dans la section `<detail>` est retourné :

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <SOAP-ENV:Fault>
         <faultcode>SOAP-ENV:Client</faultcode>
         <faultstring xml:lang="en">WARRANTY_NOT_FOUND</faultstring>
         <detail>
            <ServiceFault xmlns="http://assetpilot.com/warranty/v1">
               <errorCode>WARRANTY_NOT_FOUND</errorCode>
               <errorMessage>Aucune garantie trouvée pour l'équipement avec l'identifiant : AST-999</errorMessage>
               <assetId>AST-999</assetId>
               <timestamp>2026-11-15T22:15:00.000Z</timestamp>
            </ServiceFault>
         </detail>
      </SOAP-ENV:Fault>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

## Génération du code JAXB

La génération est automatisée lors du build via `jaxb-maven-plugin` :

```bash
mvn clean generate-sources
```

- Entrée : `src/main/resources/xsd/warranty.xsd`
- Sortie : `target/generated-sources/jaxb/com/assetpilot/warranty/soap/v1/`

---

## Compilation et tests

```bash
# Compilation et passage de la suite de tests (unitaires + intégration SOAP)
mvn clean verify
```

---

## Configuration

Fichier `src/main/resources/application.yml` :

| Propriété | Valeur par défaut | Description |
|---|---|---|
| `server.port` | `8082` | Port d'écoute HTTP du service SOAP |
| `spring.application.name` | `warranty-service` | Nom d'enregistrement du microservice |
| `spring.datasource.url` | `jdbc:h2:mem:warrantydb` | Base H2 autonome et isolée |
| `spring.h2.console.path` | `/h2-console` | Console H2 d'inspection des données |
| `ws.soap.path` | `/ws/*` | Préfixe d'accès au servlet SOAP Spring-WS |
