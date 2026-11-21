package com.assetpilot.asset_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Erreur lors d'un appel à un service distant (Warranty SOAP ou Maintenance gRPC),
 * déjà traduite en statut HTTP.
 */
public class RemoteServiceException extends RuntimeException {

    private final String service;
    private final HttpStatus status;
    private final String remoteCode;

    public RemoteServiceException(String service, HttpStatus status, String remoteCode, String message, Throwable cause) {
        super(message, cause);
        this.service = service;
        this.status = status;
        this.remoteCode = remoteCode;
    }

    public String getService() {
        return service;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getRemoteCode() {
        return remoteCode;
    }
}
