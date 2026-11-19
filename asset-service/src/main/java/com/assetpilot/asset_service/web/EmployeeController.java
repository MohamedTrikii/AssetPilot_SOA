package com.assetpilot.asset_service.web;

import com.assetpilot.asset_service.dto.EmployeeDtos.EmployeeRequest;
import com.assetpilot.asset_service.dto.EmployeeDtos.EmployeeResponse;
import com.assetpilot.asset_service.service.EmployeeService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees", description = "Gestion des employés")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Lister les employés")
    public List<EmployeeResponse> list() {
        return employeeService.findAll().stream().map(EmployeeResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un employé")
    public EmployeeResponse get(@PathVariable Long id) {
        return EmployeeResponse.from(employeeService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un employé")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = EmployeeResponse.from(employeeService.create(request));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri()).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un employé")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return EmployeeResponse.from(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un employé (sans affectation)")
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }
}
