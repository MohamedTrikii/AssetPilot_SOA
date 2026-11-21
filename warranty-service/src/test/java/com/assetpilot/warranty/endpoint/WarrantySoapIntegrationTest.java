package com.assetpilot.warranty.endpoint;

import com.assetpilot.warranty.config.WebServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;
import java.util.Map;

import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.clientOrSenderFault;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;
import static org.springframework.ws.test.server.ResponseMatchers.validPayload;
import static org.springframework.ws.test.server.ResponseMatchers.xpath;

/**
 * Test d'intégration SOAP de bout en bout via MockWebServiceClient :
 * - Sérialisation / Désérialisation des enveloppes SOAP
 * - Validation stricte du schéma XSD
 * - Vérification des réponses et des SOAP Faults
 */
@SpringBootTest
class WarrantySoapIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    private MockWebServiceClient mockClient;
    private final Resource xsdSchema = new ClassPathResource("xsd/warranty.xsd");

    @BeforeEach
    void setup() {
        mockClient = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    @DisplayName("SOAP CheckWarrantyRequest valide : retourne CheckWarrantyResponse conforme au schéma XSD")
    void testSoapCheckWarrantyValid() throws Exception {
        Source requestPayload = new StringSource(
                "<CheckWarrantyRequest xmlns=\"http://assetpilot.com/warranty/v1\">" +
                "  <assetId>AST-001</assetId>" +
                "</CheckWarrantyRequest>"
        );

        mockClient.sendRequest(withPayload(requestPayload))
                .andExpect(noFault())
                .andExpect(validPayload(xsdSchema))
                .andExpect(xpath("//ns:CheckWarrantyResponse/ns:isCovered", namespaces()).evaluatesTo(true))
                .andExpect(xpath("//ns:CheckWarrantyResponse/ns:assetId", namespaces()).evaluatesTo("AST-001"))
                .andExpect(xpath("//ns:CheckWarrantyResponse/ns:status", namespaces()).evaluatesTo("ACTIVE"));
    }

    @Test
    @DisplayName("SOAP GetWarrantyDetailsRequest valide : retourne le détail complet du contrat")
    void testSoapGetWarrantyDetailsValid() throws Exception {
        Source requestPayload = new StringSource(
                "<GetWarrantyDetailsRequest xmlns=\"http://assetpilot.com/warranty/v1\">" +
                "  <assetId>AST-001</assetId>" +
                "</GetWarrantyDetailsRequest>"
        );

        mockClient.sendRequest(withPayload(requestPayload))
                .andExpect(noFault())
                .andExpect(validPayload(xsdSchema))
                .andExpect(xpath("//ns:GetWarrantyDetailsResponse/ns:warranty/ns:contractNumber", namespaces()).evaluatesTo("DELL-PRO-2024-001"))
                .andExpect(xpath("//ns:GetWarrantyDetailsResponse/ns:warranty/ns:provider", namespaces()).evaluatesTo("Dell Technologies Partner Care"))
                .andExpect(xpath("//ns:GetWarrantyDetailsResponse/ns:warranty/ns:slaLevel", namespaces()).evaluatesTo("24/7 4h On-site Support"));
    }

    @Test
    @DisplayName("SOAP CheckWarrantyRequest pour équipement inconnu : génère un SOAP Fault Client")
    void testSoapCheckWarrantyNotFoundFault() {
        Source requestPayload = new StringSource(
                "<CheckWarrantyRequest xmlns=\"http://assetpilot.com/warranty/v1\">" +
                "  <assetId>AST-NONEXISTENT</assetId>" +
                "</CheckWarrantyRequest>"
        );

        mockClient.sendRequest(withPayload(requestPayload))
                .andExpect(clientOrSenderFault());
    }

    private static Map<String, String> namespaces() {
        return Map.of("ns", WebServiceConfig.NAMESPACE_URI);
    }
}
