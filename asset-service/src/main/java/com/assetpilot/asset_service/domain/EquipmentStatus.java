package com.assetpilot.asset_service.domain;

/**
 * Statut d'un équipement dans le parc.
 * ASSIGNED et IN_MAINTENANCE sont gérés par les règles métier (affectations, maintenance).
 */
public enum EquipmentStatus {
    AVAILABLE,
    ASSIGNED,
    IN_MAINTENANCE,
    RETIRED
}
