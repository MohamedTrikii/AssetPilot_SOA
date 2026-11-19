package com.assetpilot.asset_service.client;

import com.assetpilot.asset_service.client.warranty.soap.CheckWarrantyRequest;
import com.assetpilot.asset_service.client.warranty.soap.CheckWarrantyResponse;
import com.assetpilot.asset_service.client.warranty.soap.GetWarrantyDetailsRequest;
import com.assetpilot.asset_service.client.warranty.soap.GetWarrantyDetailsResponse;
import com.assetpilot.asset_service.client.warranty.soap.ServiceFault;
import com.assetpilot.asset_service.dto.MaintenanceDtos.WarrantyCoverage;
import com.assetpilot.asset_service.dto.MaintenanceDtos.WarrantyDetails;
import com.assetpilot.asset_service.exception.RemoteServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.WebServiceException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.client.SoapFaultClientException;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.Iterator;

/**
 * Client SOAP du Warranty Service (opérations CheckWarranty et GetWarrantyDetails).
 * Les SOAP Faults sont décodés (élément ServiceFault du &lt;detail&gt;) et traduits en erreurs HTTP.
 */
@Component
public class WarrantySoapClient {

    public static final String SERVICE_NAME = "warranty-service";
    private static final String WARRANTY_NOT_FOUND = "WARRANTY_NOT_FOUND";

    private static final Logger log = LoggerFactory.getLogger(WarrantySoapClient.class);

    private final WebServiceTemplate template;

    public WarrantySoapClient(WebServiceTemplate warrantyWebServiceTemplate) {
        this.template = warrantyWebServiceTemplate;
    }

    /**
     * Vérifie la couverture de garantie. Une garantie inexistante n'est pas une erreur :
     * la réponse indique {@code covered=false} et {@code status=NOT_FOUND}.
     */
    public WarrantyCoverage checkWarranty(String assetTag) {
        CheckWarrantyRequest request = new CheckWarrantyRequest();
        request.setAssetId(assetTag);
        try {
            CheckWarrantyResponse response = (CheckWarrantyResponse) template.marshalSendAndReceive(request);
            return new WarrantyCoverage(
                    response.getAssetId(),
                    response.isIsCovered(),
                    response.getStatus() != null ? response.getStatus().value() : null,
                    toLocalDate(response.getExpiryDate()),
                    response.getCoverageType() != null ? response.getCoverageType().value() : null,
                    response.getMessage(),
                    true);
        } catch (SoapFaultClientException fault) {
            ServiceFault detail = extractServiceFault(fault);
            if (isNotFound(fault, detail)) {
                return new WarrantyCoverage(assetTag, false, "NOT_FOUND", null, null,
                        detail != null ? detail.getErrorMessage() : "Aucune garantie enregistrée pour " + assetTag,
                        true);
            }
            throw translateFault(fault, detail);
        } catch (WebServiceException e) {
            throw translateTransport(e);
        }
    }

    /**
     * Variante résiliente utilisée avant une intervention : si le Warranty Service est indisponible
     * ou trop lent, on poursuit avec une couverture "UNKNOWN" (méthode de repli).
     */
    public WarrantyCoverage checkWarrantyOrFallback(String assetTag) {
        try {
            return checkWarranty(assetTag);
        } catch (RemoteServiceException e) {
            log.warn("Vérification de garantie impossible pour {} : {} — repli UNKNOWN", assetTag, e.getMessage());
            return new WarrantyCoverage(assetTag, false, "UNKNOWN", null, null,
                    "Garantie non vérifiée : " + e.getMessage(), false);
        }
    }

    public WarrantyDetails getWarrantyDetails(String assetTag) {
        GetWarrantyDetailsRequest request = new GetWarrantyDetailsRequest();
        request.setAssetId(assetTag);
        try {
            GetWarrantyDetailsResponse response = (GetWarrantyDetailsResponse) template.marshalSendAndReceive(request);
            var w = response.getWarranty();
            return new WarrantyDetails(w.getId(), w.getAssetId(), w.getContractNumber(), w.getProvider(),
                    toLocalDate(w.getStartDate()), toLocalDate(w.getEndDate()),
                    w.getStatus() != null ? w.getStatus().value() : null,
                    w.getCoverageType() != null ? w.getCoverageType().value() : null,
                    w.getSlaLevel(), w.getTerms(), w.getSupportContact());
        } catch (SoapFaultClientException fault) {
            throw translateFault(fault, extractServiceFault(fault));
        } catch (WebServiceException e) {
            throw translateTransport(e);
        }
    }

    private RemoteServiceException translateFault(SoapFaultClientException fault, ServiceFault detail) {
        String code = detail != null ? detail.getErrorCode() : fault.getFaultStringOrReason();
        String message = detail != null ? detail.getErrorMessage() : fault.getFaultStringOrReason();
        HttpStatus status;
        if (isNotFound(fault, detail)) {
            status = HttpStatus.NOT_FOUND;
        } else if ("INVALID_WARRANTY_REQUEST".equals(code)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.BAD_GATEWAY;
        }
        return new RemoteServiceException(SERVICE_NAME, status, code, message, fault);
    }

    private RemoteServiceException translateTransport(WebServiceException e) {
        if (e instanceof WebServiceIOException && hasCause(e, SocketTimeoutException.class)) {
            return new RemoteServiceException(SERVICE_NAME, HttpStatus.GATEWAY_TIMEOUT, "TIMEOUT",
                    "Le Warranty Service n'a pas répondu dans le délai imparti", e);
        }
        if (e instanceof WebServiceIOException || hasCause(e, IOException.class)) {
            return new RemoteServiceException(SERVICE_NAME, HttpStatus.SERVICE_UNAVAILABLE, "UNAVAILABLE",
                    "Le Warranty Service est injoignable", e);
        }
        return new RemoteServiceException(SERVICE_NAME, HttpStatus.BAD_GATEWAY, "SOAP_ERROR",
                "Réponse SOAP invalide du Warranty Service : " + e.getMessage(), e);
    }

    private static boolean isNotFound(SoapFaultClientException fault, ServiceFault detail) {
        return detail != null
                ? WARRANTY_NOT_FOUND.equals(detail.getErrorCode())
                : WARRANTY_NOT_FOUND.equals(fault.getFaultStringOrReason());
    }

    /** Décode l'élément &lt;ServiceFault&gt; contenu dans le &lt;detail&gt; du SOAP Fault. */
    private ServiceFault extractServiceFault(SoapFaultClientException fault) {
        try {
            SoapFaultDetail detail = fault.getSoapFault().getFaultDetail();
            if (detail == null) {
                return null;
            }
            Iterator<SoapFaultDetailElement> entries = detail.getDetailEntries();
            if (!entries.hasNext()) {
                return null;
            }
            Object value = template.getUnmarshaller().unmarshal(entries.next().getSource());
            return value instanceof ServiceFault serviceFault ? serviceFault : null;
        } catch (IOException | RuntimeException e) {
            log.debug("Détail du SOAP Fault illisible : {}", e.getMessage());
            return null;
        }
    }

    private static boolean hasCause(Throwable e, Class<? extends Throwable> type) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    private static LocalDate toLocalDate(XMLGregorianCalendar date) {
        return date == null ? null : LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }
}
