package com.assetpilot.asset_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTOs exposés en REST pour les flux inter-services (Warranty SOAP, Maintenance gRPC).
 */
public final class MaintenanceDtos {

    private MaintenanceDtos() {
    }

    /** Statuts d'intervention exposés en REST (miroir du contrat gRPC). */
    public enum InterventionState {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    @Schema(name = "WarrantyCoverage", description = "Résultat de CheckWarranty (Warranty Service, SOAP)")
    public record WarrantyCoverage(
            String assetTag,
            @Schema(description = "true si une garantie active couvre l'équipement aujourd'hui") boolean covered,
            @Schema(example = "ACTIVE", description = "ACTIVE, EXPIRED, VOIDED, PENDING, NOT_FOUND ou UNKNOWN")
            String status,
            LocalDate expiryDate,
            String coverageType,
            String message,
            @Schema(description = "false si le Warranty Service n'a pas pu répondre (valeur de repli)")
            boolean verified
    ) {
    }

    @Schema(name = "WarrantyDetails", description = "Résultat de GetWarrantyDetails (Warranty Service, SOAP)")
    public record WarrantyDetails(
            String id,
            String assetTag,
            String contractNumber,
            String provider,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String coverageType,
            String slaLevel,
            String terms,
            String supportContact
    ) {
    }

    @Schema(name = "CreateMaintenanceRequest")
    public record CreateMaintenanceRequest(
            @Schema(example = "L'écran clignote au démarrage") @NotBlank @Size(max = 1000) String description,
            @Schema(example = "2026-12-01T09:00:00Z", description = "Optionnel : planifie directement l'intervention")
            @Future Instant scheduledAt
    ) {
    }

    @Schema(name = "ScheduleInterventionRequest")
    public record ScheduleInterventionRequest(
            @Schema(example = "2026-12-01T09:00:00Z") @NotNull @Future Instant scheduledAt
    ) {
    }

    @Schema(name = "UpdateInterventionStatusRequest")
    public record UpdateInterventionStatusRequest(
            @Schema(example = "IN_PROGRESS") @NotNull InterventionState status
    ) {
    }

    @Schema(name = "MaintenanceRequestInfo")
    public record MaintenanceRequestInfo(
            String id,
            String assetTag,
            String description,
            Instant createdAt,
            String status
    ) {
    }

    @Schema(name = "InterventionInfo")
    public record InterventionInfo(
            String id,
            String maintenanceRequestId,
            String assetTag,
            Instant scheduledAt,
            InterventionState status
    ) {
    }

    @Schema(name = "InterventionStatusEvent")
    public record InterventionStatusEvent(
            String interventionId,
            InterventionState previousStatus,
            InterventionState currentStatus,
            Instant changedAt
    ) {
    }

    @Schema(name = "MaintenanceTicket",
            description = "Résultat du flux Asset → Warranty (SOAP) → Maintenance (gRPC)")
    public record MaintenanceTicket(
            Long equipmentId,
            String assetTag,
            WarrantyCoverage warranty,
            MaintenanceRequestInfo maintenanceRequest,
            @Schema(description = "Présente uniquement si scheduledAt a été fourni") InterventionInfo intervention
    ) {
    }
}
