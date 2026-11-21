package com.assetpilot.asset_service.dto;

import com.assetpilot.asset_service.domain.Equipment;
import com.assetpilot.asset_service.domain.EquipmentStatus;
import com.assetpilot.asset_service.domain.EquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class EquipmentDtos {

    private EquipmentDtos() {
    }

    @Schema(name = "EquipmentRequest", description = "Création ou mise à jour d'un équipement")
    public record EquipmentRequest(
            @Schema(example = "AST-010", description = "Identifiant d'inventaire partagé avec Maintenance et Warranty")
            @NotBlank @Size(max = 64) @Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "caractères autorisés : lettres, chiffres, - et _")
            String assetTag,

            @Schema(example = "Laptop Dell Latitude 5440")
            @NotBlank @Size(max = 120)
            String name,

            @Schema(example = "LAPTOP")
            @NotNull
            EquipmentType type,

            @Schema(example = "Dell") @Size(max = 60)
            String brand,

            @Schema(example = "Latitude 5440") @Size(max = 60)
            String model,

            @Schema(example = "SN-DL-5440-010")
            @NotBlank @Size(max = 80)
            String serialNumber,

            @Schema(example = "2025-01-15")
            @PastOrPresent
            LocalDate purchaseDate,

            @Schema(description = "Optionnel en mise à jour : seuls AVAILABLE et RETIRED peuvent être positionnés manuellement")
            EquipmentStatus status
    ) {
    }

    @Schema(name = "EquipmentResponse")
    public record EquipmentResponse(
            Long id,
            String assetTag,
            String name,
            EquipmentType type,
            String brand,
            String model,
            String serialNumber,
            LocalDate purchaseDate,
            EquipmentStatus status
    ) {
        public static EquipmentResponse from(Equipment e) {
            return new EquipmentResponse(e.getId(), e.getAssetTag(), e.getName(), e.getType(), e.getBrand(),
                    e.getModel(), e.getSerialNumber(), e.getPurchaseDate(), e.getStatus());
        }
    }
}
