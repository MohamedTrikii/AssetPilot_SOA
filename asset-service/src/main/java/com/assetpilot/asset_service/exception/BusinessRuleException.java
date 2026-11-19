package com.assetpilot.asset_service.exception;

/**
 * Violation d'une règle métier ou d'une contrainte d'unicité (HTTP 409).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
