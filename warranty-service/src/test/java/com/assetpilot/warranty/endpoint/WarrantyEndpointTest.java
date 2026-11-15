package com.assetpilot.warranty.endpoint;

import com.assetpilot.warranty.exception.InvalidContractException;
import com.assetpilot.warranty.exception.WarrantyNotFoundException;
import com.assetpilot.warranty.soap.v1.CheckWarrantyRequest;
import com.assetpilot.warranty.soap.v1.CheckWarrantyResponse;
import com.assetpilot.warranty.soap.v1.CoverageType;
import com.assetpilot.warranty.soap.v1.GetWarrantyDetailsRequest;
import com.assetpilot.warranty.soap.v1.GetWarrantyDetailsResponse;
import com.assetpilot.warranty.soap.v1.WarrantyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WarrantyEndpointTest {

    @Autowired
    private WarrantyEndpoint warrantyEndpoint;

    @Test
    @DisplayName("CheckWarranty : équipement sous garantie active (AST-001)")
    void testCheckWarranty_ActiveCovered() {
        CheckWarrantyRequest request = new CheckWarrantyRequest();
        request.setAssetId("AST-001");

        CheckWarrantyResponse response = warrantyEndpoint.checkWarranty(request);

        assertNotNull(response);
        assertEquals("AST-001", response.getAssetId());
        assertTrue(response.isIsCovered());
        assertEquals(WarrantyStatus.ACTIVE, response.getStatus());
        assertEquals(CoverageType.PREMIUM, response.getCoverageType());
        assertNotNull(response.getExpiryDate());
        assertTrue(response.getMessage().contains("sous garantie valide"));
    }

    @Test
    @DisplayName("CheckWarranty : équipement avec garantie expirée (AST-002)")
    void testCheckWarranty_Expired() {
        CheckWarrantyRequest request = new CheckWarrantyRequest();
        request.setAssetId("AST-002");

        CheckWarrantyResponse response = warrantyEndpoint.checkWarranty(request);

        assertNotNull(response);
        assertEquals("AST-002", response.getAssetId());
        assertFalse(response.isIsCovered());
        assertEquals(WarrantyStatus.EXPIRED, response.getStatus());
        assertEquals(CoverageType.STANDARD, response.getCoverageType());
        assertTrue(response.getMessage().contains("non couvert"));
    }

    @Test
    @DisplayName("CheckWarranty : équipement inconnu déclenche WarrantyNotFoundException")
    void testCheckWarranty_NotFound() {
        CheckWarrantyRequest request = new CheckWarrantyRequest();
        request.setAssetId("UNKNOWN-999");

        assertThrows(WarrantyNotFoundException.class, () -> {
            warrantyEndpoint.checkWarranty(request);
        });
    }

    @Test
    @DisplayName("CheckWarranty : assetId vide déclenche InvalidContractException")
    void testCheckWarranty_InvalidId() {
        CheckWarrantyRequest request = new CheckWarrantyRequest();
        request.setAssetId("   ");

        assertThrows(InvalidContractException.class, () -> {
            warrantyEndpoint.checkWarranty(request);
        });
    }

    @Test
    @DisplayName("GetWarrantyDetails : consultation détaillée d'un contrat valide (AST-001)")
    void testGetWarrantyDetails_Success() {
        GetWarrantyDetailsRequest request = new GetWarrantyDetailsRequest();
        request.setAssetId("AST-001");

        GetWarrantyDetailsResponse response = warrantyEndpoint.getWarrantyDetails(request);

        assertNotNull(response);
        assertNotNull(response.getWarranty());
        assertEquals("WAR-1001", response.getWarranty().getId());
        assertEquals("AST-001", response.getWarranty().getAssetId());
        assertEquals("DELL-PRO-2024-001", response.getWarranty().getContractNumber());
        assertEquals("Dell Technologies Partner Care", response.getWarranty().getProvider());
        assertEquals(WarrantyStatus.ACTIVE, response.getWarranty().getStatus());
        assertEquals(CoverageType.PREMIUM, response.getWarranty().getCoverageType());
        assertEquals("24/7 4h On-site Support", response.getWarranty().getSlaLevel());
        assertEquals("support-dell@partner.assetpilot.com", response.getWarranty().getSupportContact());
    }

    @Test
    @DisplayName("GetWarrantyDetails : équipement inconnu déclenche WarrantyNotFoundException")
    void testGetWarrantyDetails_NotFound() {
        GetWarrantyDetailsRequest request = new GetWarrantyDetailsRequest();
        request.setAssetId("UNKNOWN-404");

        assertThrows(WarrantyNotFoundException.class, () -> {
            warrantyEndpoint.getWarrantyDetails(request);
        });
    }

    @Test
    @DisplayName("GetWarrantyDetails : requête nulle ou vide déclenche InvalidContractException")
    void testGetWarrantyDetails_Invalid() {
        GetWarrantyDetailsRequest request = new GetWarrantyDetailsRequest();
        request.setAssetId(null);

        assertThrows(InvalidContractException.class, () -> {
            warrantyEndpoint.getWarrantyDetails(request);
        });
    }
}
