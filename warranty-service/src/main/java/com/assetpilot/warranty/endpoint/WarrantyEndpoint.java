package com.assetpilot.warranty.endpoint;

import com.assetpilot.warranty.config.WebServiceConfig;
import com.assetpilot.warranty.exception.InvalidContractException;
import com.assetpilot.warranty.model.WarrantyEntity;
import com.assetpilot.warranty.service.WarrantyService;
import com.assetpilot.warranty.soap.v1.CheckWarrantyRequest;
import com.assetpilot.warranty.soap.v1.CheckWarrantyResponse;
import com.assetpilot.warranty.soap.v1.CoverageType;
import com.assetpilot.warranty.soap.v1.GetWarrantyDetailsRequest;
import com.assetpilot.warranty.soap.v1.GetWarrantyDetailsResponse;
import com.assetpilot.warranty.soap.v1.WarrantyDetails;
import com.assetpilot.warranty.soap.v1.WarrantyStatus;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

/**
 * Endpoint SOAP exposant les opérations de gestion de garanties :
 * 1. CheckWarranty : vérifie la couverture et la validité de la garantie d'un équipement.
 * 2. GetWarrantyDetails : fournit le détail complet du contrat de garantie.
 */
@Endpoint
public class WarrantyEndpoint {

    private final WarrantyService warrantyService;

    public WarrantyEndpoint(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE_URI, localPart = "CheckWarrantyRequest")
    @ResponsePayload
    public CheckWarrantyResponse checkWarranty(@RequestPayload CheckWarrantyRequest request) {
        if (request == null || request.getAssetId() == null || request.getAssetId().trim().isEmpty()) {
            throw new InvalidContractException("La requête CheckWarrantyRequest doit contenir un 'assetId' valide.");
        }

        WarrantyEntity entity = warrantyService.getWarrantyByAssetId(request.getAssetId());
        boolean isCovered = entity.isCovered();

        CheckWarrantyResponse response = new CheckWarrantyResponse();
        response.setAssetId(entity.getAssetId());
        response.setIsCovered(isCovered);
        response.setStatus(WarrantyStatus.fromValue(entity.getStatus().name()));
        response.setCoverageType(CoverageType.fromValue(entity.getCoverageType().name()));
        response.setExpiryDate(toXmlDate(entity.getEndDate()));

        if (isCovered) {
            response.setMessage("Équipement sous garantie valide jusqu'au " + entity.getEndDate() 
                    + " (" + entity.getProvider() + " - " + entity.getSlaLevel() + ")");
        } else {
            response.setMessage("Équipement non couvert par une garantie active (statut: " 
                    + entity.getStatus() + ", expiration: " + entity.getEndDate() + ")");
        }

        return response;
    }

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE_URI, localPart = "GetWarrantyDetailsRequest")
    @ResponsePayload
    public GetWarrantyDetailsResponse getWarrantyDetails(@RequestPayload GetWarrantyDetailsRequest request) {
        if (request == null || request.getAssetId() == null || request.getAssetId().trim().isEmpty()) {
            throw new InvalidContractException("La requête GetWarrantyDetailsRequest doit contenir un 'assetId' valide.");
        }

        WarrantyEntity entity = warrantyService.getWarrantyByAssetId(request.getAssetId());

        WarrantyDetails details = new WarrantyDetails();
        details.setId(entity.getId());
        details.setAssetId(entity.getAssetId());
        details.setContractNumber(entity.getContractNumber());
        details.setProvider(entity.getProvider());
        details.setStartDate(toXmlDate(entity.getStartDate()));
        details.setEndDate(toXmlDate(entity.getEndDate()));
        details.setStatus(WarrantyStatus.fromValue(entity.getStatus().name()));
        details.setCoverageType(CoverageType.fromValue(entity.getCoverageType().name()));
        details.setSlaLevel(entity.getSlaLevel());
        details.setTerms(entity.getTerms());
        details.setSupportContact(entity.getSupportContact());

        GetWarrantyDetailsResponse response = new GetWarrantyDetailsResponse();
        response.setWarranty(details);
        return response;
    }

    private XMLGregorianCalendar toXmlDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
                    date.getYear(),
                    date.getMonthValue(),
                    date.getDayOfMonth(),
                    DatatypeConstants.FIELD_UNDEFINED
            );
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Impossible de convertir la date en XMLGregorianCalendar : " + date, e);
        }
    }
}
