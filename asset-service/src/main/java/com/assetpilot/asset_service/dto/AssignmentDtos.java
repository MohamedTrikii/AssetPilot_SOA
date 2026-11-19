package com.assetpilot.asset_service.dto;

import com.assetpilot.asset_service.domain.Assignment;
import com.assetpilot.asset_service.domain.AssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class AssignmentDtos {

    private AssignmentDtos() {
    }

    @Schema(name = "AssignmentRequest")
    public record AssignmentRequest(
            @Schema(example = "2") @NotNull Long equipmentId,
            @Schema(example = "1") @NotNull Long employeeId,
            @Schema(example = "2026-11-19", description = "Par défaut : aujourd'hui") @PastOrPresent LocalDate assignedAt,
            @Schema(example = "Poste de travail principal") @Size(max = 500) String notes
    ) {
    }

    @Schema(name = "ReturnAssignmentRequest")
    public record ReturnAssignmentRequest(
            @Schema(example = "2026-12-01", description = "Par défaut : aujourd'hui") @PastOrPresent LocalDate returnedAt,
            @Size(max = 500) String notes
    ) {
    }

    @Schema(name = "AssignmentResponse")
    public record AssignmentResponse(
            Long id,
            Long equipmentId,
            String assetTag,
            Long employeeId,
            String employeeName,
            LocalDate assignedAt,
            LocalDate returnedAt,
            AssignmentStatus status,
            String notes
    ) {
        public static AssignmentResponse from(Assignment a) {
            return new AssignmentResponse(a.getId(), a.getEquipment().getId(), a.getEquipment().getAssetTag(),
                    a.getEmployee().getId(), a.getEmployee().getFullName(), a.getAssignedAt(), a.getReturnedAt(),
                    a.getStatus(), a.getNotes());
        }
    }
}
