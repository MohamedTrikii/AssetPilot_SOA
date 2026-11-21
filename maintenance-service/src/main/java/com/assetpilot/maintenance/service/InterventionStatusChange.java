package com.assetpilot.maintenance.service;

import com.assetpilot.maintenance.model.InterventionStatus;

import java.time.Instant;

/**
 * Événement métier : changement de statut d'une intervention.
 * {@code previousStatus} vaut null pour l'état initial envoyé à l'abonnement.
 */
public record InterventionStatusChange(
        String interventionId,
        InterventionStatus previousStatus,
        InterventionStatus currentStatus,
        Instant changedAt
) {
}
