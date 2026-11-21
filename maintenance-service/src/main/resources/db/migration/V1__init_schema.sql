-- =====================================================================
-- Maintenance Service — base maintenancedb
-- Schéma initial : demandes de maintenance et interventions.
-- asset_id est l'identifiant métier (assetTag) partagé avec les autres
-- services : pas de clé étrangère inter-bases.
-- SQL portable PostgreSQL / H2 (mode local et tests).
-- =====================================================================

CREATE TABLE maintenance_requests (
    id           VARCHAR(36)   NOT NULL,
    asset_id     VARCHAR(64)   NOT NULL,
    description  VARCHAR(1000) NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    CONSTRAINT pk_maintenance_requests PRIMARY KEY (id),
    CONSTRAINT ck_maintenance_requests_status CHECK (status IN ('OPEN', 'SCHEDULED', 'RESOLVED', 'CANCELLED'))
);

CREATE INDEX idx_maintenance_requests_asset ON maintenance_requests (asset_id);

CREATE TABLE interventions (
    id                      VARCHAR(36) NOT NULL,
    maintenance_request_id  VARCHAR(36) NOT NULL,
    asset_id                VARCHAR(64) NOT NULL,
    scheduled_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status                  VARCHAR(20) NOT NULL,
    CONSTRAINT pk_interventions PRIMARY KEY (id),
    CONSTRAINT fk_interventions_request FOREIGN KEY (maintenance_request_id)
        REFERENCES maintenance_requests (id),
    CONSTRAINT ck_interventions_status CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_interventions_request ON interventions (maintenance_request_id);
CREATE INDEX idx_interventions_asset ON interventions (asset_id);
