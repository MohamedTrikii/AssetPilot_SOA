package com.assetpilot.asset_service.web;

import com.assetpilot.asset_service.domain.EquipmentStatus;
import com.assetpilot.asset_service.domain.EquipmentType;
import com.assetpilot.asset_service.dto.EquipmentDtos.EquipmentRequest;
import com.assetpilot.asset_service.dto.EquipmentDtos.EquipmentResponse;
import com.assetpilot.asset_service.dto.MaintenanceDtos.CreateMaintenanceRequest;
import com.assetpilot.asset_service.dto.MaintenanceDtos.MaintenanceTicket;
import com.assetpilot.asset_service.dto.MaintenanceDtos.WarrantyCoverage;
import com.assetpilot.asset_service.dto.MaintenanceDtos.WarrantyDetails;
import com.assetpilot.asset_service.service.EquipmentService;
import com.assetpilot.asset_service.service.MaintenanceOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipments")
@Tag(name = "Equipments", description = "Gestion des équipements du parc informatique")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final MaintenanceOrchestrationService orchestrationService;

    public EquipmentController(EquipmentService equipmentService, MaintenanceOrchestrationService orchestrationService) {
        this.equipmentService = equipmentService;
        this.orchestrationService = orchestrationService;
    }

    @GetMapping
    @Operation(summary = "Lister les équipements (filtres optionnels : statut, type)")
    public List<EquipmentResponse> list(@RequestParam(required = false) EquipmentStatus status,
                                        @RequestParam(required = false) EquipmentType type) {
        return equipmentService.findAll(status, type).stream().map(EquipmentResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un équipement")
    public EquipmentResponse get(@PathVariable Long id) {
        return EquipmentResponse.from(equipmentService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un équipement")
    public ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request) {
        EquipmentResponse created = EquipmentResponse.from(equipmentService.create(request));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri()).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un équipement")
    public EquipmentResponse update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return EquipmentResponse.from(equipmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un équipement (sans historique d'affectation)")
    public void delete(@PathVariable Long id) {
        equipmentService.delete(id);
    }

    // ----------------------------------------------------- Flux inter-services

    @GetMapping("/{id}/warranty")
    @Operation(summary = "Vérifier la garantie (Warranty Service — SOAP CheckWarranty)")
    public WarrantyCoverage checkWarranty(@PathVariable Long id) {
        return orchestrationService.checkWarranty(id);
    }

    @GetMapping("/{id}/warranty/details")
    @Operation(summary = "Détails du contrat de garantie (Warranty Service — SOAP GetWarrantyDetails)")
    public WarrantyDetails warrantyDetails(@PathVariable Long id) {
        return orchestrationService.getWarrantyDetails(id);
    }

    @PostMapping("/{id}/maintenance-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Déclarer une panne : vérifie la garantie (SOAP) puis crée/planifie l'intervention (gRPC)")
    public MaintenanceTicket openMaintenance(@PathVariable Long id, @Valid @RequestBody CreateMaintenanceRequest request) {
        return orchestrationService.openMaintenance(id, request);
    }
}
