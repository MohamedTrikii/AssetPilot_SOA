package com.assetpilot.asset_service.dto;

import com.assetpilot.asset_service.domain.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    @Schema(name = "EmployeeRequest")
    public record EmployeeRequest(
            @Schema(example = "Sarra") @NotBlank @Size(max = 60) String firstName,
            @Schema(example = "Ben Salah") @NotBlank @Size(max = 60) String lastName,
            @Schema(example = "sarra.bensalah@assetpilot.tn") @NotBlank @Email @Size(max = 120) String email,
            @Schema(example = "Finance") @Size(max = 80) String department,
            @Schema(example = "Comptable") @Size(max = 80) String jobTitle
    ) {
    }

    @Schema(name = "EmployeeResponse")
    public record EmployeeResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String department,
            String jobTitle
    ) {
        public static EmployeeResponse from(Employee e) {
            return new EmployeeResponse(e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(),
                    e.getDepartment(), e.getJobTitle());
        }
    }
}
