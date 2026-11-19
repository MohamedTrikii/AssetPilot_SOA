package com.assetpilot.maintenance.grpc;

import com.assetpilot.maintenance.service.MaintenanceException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traduction centralisée des exceptions en statuts gRPC standards.
 */
@GrpcAdvice
public class GrpcExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(GrpcExceptionAdvice.class);

    @GrpcExceptionHandler(MaintenanceException.class)
    public Status handleBusiness(MaintenanceException e) {
        Status status = switch (e.getReason()) {
            case INVALID_ARGUMENT -> Status.INVALID_ARGUMENT;
            case NOT_FOUND -> Status.NOT_FOUND;
            case FAILED_PRECONDITION -> Status.FAILED_PRECONDITION;
        };
        return status.withDescription(e.getMessage());
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleUnexpected(Exception e) {
        log.error("Erreur interne du Maintenance Service", e);
        return Status.INTERNAL.withDescription("Erreur interne du Maintenance Service");
    }
}
