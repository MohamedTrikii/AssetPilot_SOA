-- =====================================================================
-- Warranty Service — base warrantydb
-- Schéma initial : garanties des équipements (une garantie par assetTag).
-- Le jeu de garanties de démonstration est inséré au démarrage par
-- WarrantyApplication#initData (dates relatives à la date du jour),
-- uniquement si la table est vide.
-- SQL portable PostgreSQL / H2 (mode local et tests).
-- =====================================================================

CREATE TABLE warranties (
    id               VARCHAR(64)  NOT NULL,
    asset_id         VARCHAR(64)  NOT NULL,
    contract_number  VARCHAR(64)  NOT NULL,
    provider         VARCHAR(100) NOT NULL,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    coverage_type    VARCHAR(20)  NOT NULL,
    sla_level        VARCHAR(100) NOT NULL,
    terms            VARCHAR(500),
    support_contact  VARCHAR(100),
    CONSTRAINT pk_warranties PRIMARY KEY (id),
    CONSTRAINT uk_warranties_asset_id UNIQUE (asset_id),
    CONSTRAINT uk_warranties_contract_number UNIQUE (contract_number),
    CONSTRAINT ck_warranties_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'VOIDED', 'PENDING')),
    CONSTRAINT ck_warranties_coverage_type CHECK (coverage_type IN
        ('STANDARD', 'PREMIUM', 'EXTENDED', 'PARTS_ONLY', 'LABOR_ONLY')),
    CONSTRAINT ck_warranties_dates CHECK (end_date >= start_date)
);
