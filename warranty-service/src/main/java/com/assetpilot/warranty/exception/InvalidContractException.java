package com.assetpilot.warranty.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Exception levée lorsqu'une requête de garantie est invalide (paramètre manquant ou format incorrect).
 * Mappée en SOAP Fault avec code Client.
 */
@SoapFault(faultCode = FaultCode.CLIENT, faultStringOrReason = "INVALID_WARRANTY_REQUEST")
public class InvalidContractException extends RuntimeException {

    public InvalidContractException(String message) {
        super(message);
    }
}
