package com.assetpilot.maintenance.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Cycle de vie d'une intervention : SCHEDULED -> IN_PROGRESS -> COMPLETED, ou CANCELLED.
 */
public enum InterventionStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    /** Transitions autorisées depuis ce statut. */
    public Set<InterventionStatus> allowedTransitions() {
        return switch (this) {
            case SCHEDULED -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(InterventionStatus.class);
        };
    }

    public boolean canTransitionTo(InterventionStatus target) {
        return allowedTransitions().contains(target);
    }

    /** Un statut terminal clôt le flux de suivi. */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
