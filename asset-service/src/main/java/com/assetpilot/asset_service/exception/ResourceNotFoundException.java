package com.assetpilot.asset_service.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " introuvable : " + id);
    }
}
