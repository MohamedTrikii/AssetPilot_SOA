package com.assetpilot.warranty.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Exception levée lorsqu'aucune garantie n'est enregistrée pour un équipement donné.
 * Génère automatiquement un SOAP Fault avec le code Client.
 */
@SoapFault(faultCode = FaultCode.CLIENT, faultStringOrReason = "WARRANTY_NOT_FOUND")
public class WarrantyNotFoundException extends RuntimeException {

    private final String assetId;

    public WarrantyNotFoundException(String assetId) {
        super("Aucune garantie trouvée pour l'équipement avec l'identifiant : " + assetId);
        this.assetId = assetId;
    }

    public String getAssetId() {
        return assetId;
    }
}
