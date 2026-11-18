package com.assetpilot.warranty.exception;

import com.assetpilot.warranty.soap.v1.ServiceFault;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

/**
 * Résolveur d'exceptions SOAP Fault ajoutant un élément ServiceFault conforme au schéma XSD
 * dans la section <detail> du SOAP Fault.
 */
public class DetailSoapFaultDefinitionExceptionResolver extends SoapFaultAnnotationExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(DetailSoapFaultDefinitionExceptionResolver.class);

    private final JAXBContext jaxbContext;

    public DetailSoapFaultDefinitionExceptionResolver() {
        try {
            this.jaxbContext = JAXBContext.newInstance(ServiceFault.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Impossible d'initialiser le JAXBContext pour ServiceFault", e);
        }
    }

    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        SoapFaultDetail detail = fault.addFaultDetail();
        ServiceFault serviceFault = new ServiceFault();
        serviceFault.setTimestamp(getCurrentXmlDateTime());

        if (ex instanceof WarrantyNotFoundException wnfe) {
            serviceFault.setErrorCode("WARRANTY_NOT_FOUND");
            serviceFault.setErrorMessage(wnfe.getMessage());
            serviceFault.setAssetId(wnfe.getAssetId());
        } else if (ex instanceof InvalidContractException ice) {
            serviceFault.setErrorCode("INVALID_WARRANTY_REQUEST");
            serviceFault.setErrorMessage(ice.getMessage());
        } else {
            serviceFault.setErrorCode("INTERNAL_SERVER_ERROR");
            serviceFault.setErrorMessage(ex.getMessage() != null ? ex.getMessage() : "Une erreur interne est survenue.");
        }

        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(serviceFault, detail.getResult());
        } catch (JAXBException e) {
            log.error("Erreur lors de la sérialisation du ServiceFault dans le SOAP detail", e);
        }
    }

    private XMLGregorianCalendar getCurrentXmlDateTime() {
        try {
            GregorianCalendar calendar = GregorianCalendar.from(ZonedDateTime.now());
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }
}
