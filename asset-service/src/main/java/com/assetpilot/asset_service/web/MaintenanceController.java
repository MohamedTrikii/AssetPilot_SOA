package com.assetpilot.asset_service.web;

import com.assetpilot.asset_service.client.MaintenanceGrpcClient;
import com.assetpilot.asset_service.config.MaintenanceClientProperties;
import com.assetpilot.asset_service.dto.MaintenanceDtos.InterventionInfo;
import com.assetpilot.asset_service.dto.MaintenanceDtos.InterventionState;
import com.assetpilot.asset_service.dto.MaintenanceDtos.ScheduleInterventionRequest;
import com.assetpilot.asset_service.dto.MaintenanceDtos.UpdateInterventionStatusRequest;
import com.assetpilot.asset_service.service.EquipmentService;
import com.assetpilot.asset_service.service.MaintenanceOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * Façade REST des opérations de maintenance, relayées vers le Maintenance Service en gRPC.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Maintenance", description = "Interventions de maintenance (relayées vers le Maintenance Service en gRPC)")
public class MaintenanceController {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceController.class);

    private final MaintenanceOrchestrationService orchestrationService;
    private final MaintenanceGrpcClient maintenanceClient;
    private final EquipmentService equipmentService;
    private final MaintenanceClientProperties properties;

    public MaintenanceController(MaintenanceOrchestrationService orchestrationService,
                                 MaintenanceGrpcClient maintenanceClient,
                                 EquipmentService equipmentService,
                                 MaintenanceClientProperties properties) {
        this.orchestrationService = orchestrationService;
        this.maintenanceClient = maintenanceClient;
        this.equipmentService = equipmentService;
        this.properties = properties;
    }

    @PostMapping("/maintenance-requests/{requestId}/interventions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Planifier une intervention pour une demande existante (gRPC ScheduleIntervention)")
    public InterventionInfo schedule(@PathVariable String requestId,
                                     @Valid @RequestBody ScheduleInterventionRequest request) {
        return orchestrationService.scheduleIntervention(requestId, request.scheduledAt());
    }

    @GetMapping("/interventions/{interventionId}")
    @Operation(summary = "Consulter une intervention (gRPC GetIntervention)")
    public InterventionInfo get(@PathVariable String interventionId) {
        return orchestrationService.getIntervention(interventionId);
    }

    @PatchMapping("/interventions/{interventionId}/status")
    @Operation(summary = "Faire évoluer le statut d'une intervention (gRPC UpdateInterventionStatus)")
    public InterventionInfo updateStatus(@PathVariable String interventionId,
                                         @Valid @RequestBody UpdateInterventionStatusRequest request) {
        return orchestrationService.updateInterventionStatus(interventionId, request.status());
    }

    /**
     * Relaie le flux gRPC server-streaming TrackInterventionStatus en Server-Sent Events.
     * Le flux se ferme quand l'intervention est COMPLETED ou CANCELLED.
     */
    @GetMapping(value = "/interventions/{interventionId}/status-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Suivre les changements de statut en temps réel (gRPC streaming → SSE)")
    public SseEmitter track(@PathVariable String interventionId) {
        // Appel unaire préalable : renvoie un vrai 404 si l'intervention n'existe pas
        String assetTag = orchestrationService.getIntervention(interventionId).assetTag();

        SseEmitter emitter = new SseEmitter(properties.streamDeadline().toMillis());
        Runnable cancel = maintenanceClient.trackInterventionStatus(interventionId,
                event -> {
                    try {
                        emitter.send(SseEmitter.event().name("status").data(event));
                        if (event.currentStatus() == InterventionState.COMPLETED
                                || event.currentStatus() == InterventionState.CANCELLED) {
                            equipmentService.releaseFromMaintenance(assetTag);
                        }
                    } catch (IOException | IllegalStateException e) {
                        log.debug("Client SSE déconnecté : {}", e.getMessage());
                    }
                },
                error -> {
                    try {
                        emitter.send(SseEmitter.event().name("error").data(Map.of(
                                "service", error.getService(),
                                "code", error.getRemoteCode(),
                                "message", error.getMessage())));
                    } catch (IOException | IllegalStateException ignored) {
                        // client déjà parti
                    }
                    emitter.complete();
                },
                emitter::complete);

        emitter.onCompletion(cancel);
        emitter.onTimeout(cancel);
        emitter.onError(e -> cancel.run());
        return emitter;
    }
}
