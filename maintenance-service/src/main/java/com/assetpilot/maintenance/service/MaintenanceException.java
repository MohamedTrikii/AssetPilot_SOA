package com.assetpilot.maintenance.service;

/**
 * Exception métier du Maintenance Service. La couche gRPC la convertit en statut gRPC
 * (INVALID_ARGUMENT, NOT_FOUND, FAILED_PRECONDITION).
 */
public class MaintenanceException extends RuntimeException {

    public enum Reason {
        INVALID_ARGUMENT,
        NOT_FOUND,
        FAILED_PRECONDITION
    }

    private final Reason reason;

    public MaintenanceException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public static MaintenanceException invalidArgument(String message) {
        return new MaintenanceException(Reason.INVALID_ARGUMENT, message);
    }

    public static MaintenanceException notFound(String message) {
        return new MaintenanceException(Reason.NOT_FOUND, message);
    }

    public static MaintenanceException failedPrecondition(String message) {
        return new MaintenanceException(Reason.FAILED_PRECONDITION, message);
    }
}
