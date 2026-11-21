package com.assetpilot.asset_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

/**
 * WebServiceTemplate configuré pour le Warranty Service :
 * marshalling JAXB (classes générées depuis warranty.xsd) et délais d'attente.
 */
@Configuration
public class WarrantyClientConfig {

    public static final String SOAP_PACKAGE = "com.assetpilot.asset_service.client.warranty.soap";

    @Bean
    public Jaxb2Marshaller warrantyMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(SOAP_PACKAGE);
        return marshaller;
    }

    @Bean
    public WebServiceTemplate warrantyWebServiceTemplate(Jaxb2Marshaller warrantyMarshaller,
                                                         WarrantyClientProperties properties) {
        WebServiceTemplate template = new WebServiceTemplate(warrantyMarshaller);
        template.setDefaultUri(properties.url());

        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(properties.connectTimeout());
        sender.setReadTimeout(properties.readTimeout());
        template.setMessageSender(sender);
        return template;
    }
}
