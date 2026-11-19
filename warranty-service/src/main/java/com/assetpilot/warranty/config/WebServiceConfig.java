package com.assetpilot.warranty.config;

import com.assetpilot.warranty.exception.DetailSoapFaultDefinitionExceptionResolver;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Configuration de l'infrastructure SOAP Spring Web Services.
 * - Enregistrement de la MessageDispatcherServlet sur /ws/*
 * - Exposition dynamique du contrat WSDL sur /ws/warranties.wsdl
 * - Résolution des exceptions en SOAP Faults enrichis
 */
@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    public static final String NAMESPACE_URI = "http://assetpilot.com/warranty/v1";

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "warranties")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema warrantySchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("WarrantyPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace(NAMESPACE_URI);
        wsdl11Definition.setSchema(warrantySchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema warrantySchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/warranty.xsd"));
    }

    @Bean
    public SoapFaultAnnotationExceptionResolver customExceptionResolver() {
        DetailSoapFaultDefinitionExceptionResolver resolver = new DetailSoapFaultDefinitionExceptionResolver();
        resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return resolver;
    }
}
