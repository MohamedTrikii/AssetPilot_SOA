package com.assetpilot.asset_service.web;

import com.assetpilot.asset_service.domain.AssignmentStatus;
import com.assetpilot.asset_service.dto.AssignmentDtos.AssignmentRequest;
import com.assetpilot.asset_service.dto.AssignmentDtos.AssignmentResponse;
import com.assetpilot.asset_service.dto.AssignmentDtos.ReturnAssignmentRequest;
import com.assetpilot.asset_service.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@Tag(name = "Assignments", description = "Affectation des équipements aux employés")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    @Operation(summary = "Lister les affectations (filtres optionnels : employé, équipement, statut)")
    public List<AssignmentResponse> list(@RequestParam(required = false) Long employeeId,
                                         @RequestParam(required = false) Long equipmentId,
                                         @RequestParam(required = false) AssignmentStatus status) {
        return assignmentService.findAll(employeeId, equipmentId, status).stream().map(AssignmentResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une affectation")
    public AssignmentResponse get(@PathVariable Long id) {
        return AssignmentResponse.from(assignmentService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Affecter un équipement disponible à un employé")
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse created = AssignmentResponse.from(assignmentService.create(request));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri()).body(created);
    }

    @PatchMapping("/{id}/return")
    @Operation(summary = "Restituer l'équipement (clôture de l'affectation)")
    public AssignmentResponse returnEquipment(@PathVariable Long id,
                                              @Valid @RequestBody(required = false) ReturnAssignmentRequest request) {
        return AssignmentResponse.from(assignmentService.returnEquipment(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une affectation clôturée")
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }
}
