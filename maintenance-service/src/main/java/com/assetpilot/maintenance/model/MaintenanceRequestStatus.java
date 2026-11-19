package com.assetpilot.maintenance.model;

/**
 * Cycle de vie d'une demande de maintenance (modèle métier, indépendant de protobuf).
 */
public enum MaintenanceRequestStatus {
    OPEN,
    SCHEDULED,
    RESOLVED,
    CANCELLED
}
