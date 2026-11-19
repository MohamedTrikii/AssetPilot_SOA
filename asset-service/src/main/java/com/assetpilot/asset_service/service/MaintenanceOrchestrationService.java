package com.assetpilot.asset_service.service;

import com.assetpilot.asset_service.client.MaintenanceGrpcClient;
import com.assetpilot.asset_service.client.WarrantySoapClient;
import com.assetpilot.asset_service.domain.Equipment;
import com.assetpilot.asset_service.domain.EquipmentStatus;
import com.assetpilot.asset_service.dto.MaintenanceDtos.CreateMaintenanceRequest;
import com.assetpilot.asset_service.dto.MaintenanceDtos.InterventionInfo;
import com.assetpilot.asset_service.dto.MaintenanceDtos.InterventionState;
import com.assetpilot.asset_service.dto.MaintenanceDtos.MaintenanceRequestInfo;
import com.assetpilot.asset_service.dto.MaintenanceDtos.MaintenanceTicket;
import com.assetpilot.asset_service.dto.MaintenanceDtos.WarrantyCoverage;
import com.assetpilot.asset_service.dto.MaintenanceDtos.WarrantyDetails;
import com.assetpilot.asset_service.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Orchestration des appels inter-services :
 * <ol>
 *   <li>Asset → Warranty (SOAP) : vérification de la couverture avant l'intervention ;</li>
 *   <li>Asset → Maintenance (gRPC) : création de la demande et planification de l'intervention.</li>
 * </ol>
 * Les appels distants ne sont pas exécutés dans une transaction base de données.
 */
@Service
public class MaintenanceOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceOrchestrationService.class);

    private final EquipmentService equipmentService;
    private final WarrantySoapClient warrantyClient;
    private final MaintenanceGrpcClient maintenanceClient;

    public MaintenanceOrchestrationService(EquipmentService equipmentService, WarrantySoapClient warrantyClient,
                                           MaintenanceGrpcClient maintenanceClient) {
        this.equipmentService = equipmentService;
        this.warrantyClient = warrantyClient;
        this.maintenanceClient = maintenanceClient;
    }

    public WarrantyCoverage checkWarranty(Long equipmentId) {
        Equipment equipment = equipmentService.findById(equipmentId);
        return warrantyClient.checkWarranty(equipment.getAssetTag());
    }

    public WarrantyDetails getWarrantyDetails(Long equipmentId) {
        Equipment equipment = equipmentService.findById(equipmentId);
        return warrantyClient.getWarrantyDetails(equipment.getAssetTag());
    }

    public MaintenanceTicket openMaintenance(Long equipmentId, CreateMaintenanceRequest request) {
        Equipment equipment = equipmentService.findById(equipmentId);
        if (equipment.getStatus() == EquipmentStatus.RETIRED) {
            throw new BusinessRuleException("L'équipement " + equipment.getAssetTag() + " est retiré du parc");
        }
        String assetTag = equipment.getAssetTag();

        // 1. SOAP : vérification de garantie (repli UNKNOWN si le service est indisponible)
        WarrantyCoverage warranty = warrantyClient.checkWarrantyOrFallback(assetTag);
        log.info("Garantie de {} : covered={} status={}", assetTag, warranty.covered(), warranty.status());

        // 2. gRPC unaire : création de la demande de maintenance
        String description = request.description().trim();
        if (!warranty.covered()) {
            description += " [Hors garantie : " + warranty.status() + "]";
        }
        MaintenanceRequestInfo maintenanceRequest = maintenanceClient.createMaintenanceRequest(assetTag, description);

        // 3. gRPC unaire (optionnel) : planification de l'intervention
        InterventionInfo intervention = null;
        if (request.scheduledAt() != null) {
            intervention = maintenanceClient.scheduleIntervention(maintenanceRequest.id(), request.scheduledAt());
            maintenanceRequest = new MaintenanceRequestInfo(maintenanceRequest.id(), maintenanceRequest.assetTag(),
                    maintenanceRequest.description(), maintenanceRequest.createdAt(), "SCHEDULED");
        }

        equipmentService.markInMaintenance(equipmentId);
        return new MaintenanceTicket(equipmentId, assetTag, warranty, maintenanceRequest, intervention);
    }

    public InterventionInfo scheduleIntervention(String maintenanceRequestId, Instant scheduledAt) {
        return maintenanceClient.scheduleIntervention(maintenanceRequestId, scheduledAt);
    }

    public InterventionInfo getIntervention(String interventionId) {
        return maintenanceClient.getIntervention(interventionId);
    }

    public InterventionInfo updateInterventionStatus(String interventionId, InterventionState status) {
        InterventionInfo updated = maintenanceClient.updateInterventionStatus(interventionId, status);
        if (updated.status() == InterventionState.COMPLETED || updated.status() == InterventionState.CANCELLED) {
            equipmentService.releaseFromMaintenance(updated.assetTag());
        }
        return updated;
    }
}
