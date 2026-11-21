package com.assetpilot.asset_service.service;

import com.assetpilot.asset_service.domain.Assignment;
import com.assetpilot.asset_service.domain.AssignmentStatus;
import com.assetpilot.asset_service.domain.Employee;
import com.assetpilot.asset_service.domain.Equipment;
import com.assetpilot.asset_service.domain.EquipmentStatus;
import com.assetpilot.asset_service.dto.AssignmentDtos.AssignmentRequest;
import com.assetpilot.asset_service.dto.AssignmentDtos.ReturnAssignmentRequest;
import com.assetpilot.asset_service.exception.BusinessRuleException;
import com.assetpilot.asset_service.exception.ResourceNotFoundException;
import com.assetpilot.asset_service.repository.AssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final EquipmentService equipmentService;
    private final EmployeeService employeeService;

    public AssignmentService(AssignmentRepository assignmentRepository, EquipmentService equipmentService,
                             EmployeeService employeeService) {
        this.assignmentRepository = assignmentRepository;
        this.equipmentService = equipmentService;
        this.employeeService = employeeService;
    }

    @Transactional(readOnly = true)
    public List<Assignment> findAll(Long employeeId, Long equipmentId, AssignmentStatus status) {
        return assignmentRepository.search(employeeId, equipmentId, status);
    }

    @Transactional(readOnly = true)
    public Assignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", id));
    }

    /** Affecte un équipement disponible à un employé ; l'équipement passe à ASSIGNED. */
    public Assignment create(AssignmentRequest request) {
        Equipment equipment = equipmentService.findById(request.equipmentId());
        Employee employee = employeeService.findById(request.employeeId());

        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new BusinessRuleException("L'équipement " + equipment.getAssetTag()
                    + " n'est pas disponible (statut : " + equipment.getStatus() + ")");
        }

        Assignment assignment = new Assignment();
        assignment.setEquipment(equipment);
        assignment.setEmployee(employee);
        assignment.setAssignedAt(request.assignedAt() != null ? request.assignedAt() : LocalDate.now());
        assignment.setNotes(request.notes());
        assignment.setStatus(AssignmentStatus.ACTIVE);
        equipment.setStatus(EquipmentStatus.ASSIGNED);
        return assignmentRepository.save(assignment);
    }

    /** Clôture une affectation ; l'équipement redevient AVAILABLE (sauf s'il est en maintenance). */
    public Assignment returnEquipment(Long id, ReturnAssignmentRequest request) {
        Assignment assignment = findById(id);
        if (assignment.getStatus() == AssignmentStatus.RETURNED) {
            throw new BusinessRuleException("L'affectation " + id + " est déjà clôturée");
        }
        LocalDate returnedAt = request != null && request.returnedAt() != null ? request.returnedAt() : LocalDate.now();
        if (returnedAt.isBefore(assignment.getAssignedAt())) {
            throw new BusinessRuleException("La date de retour ne peut pas précéder la date d'affectation");
        }
        assignment.setReturnedAt(returnedAt);
        assignment.setStatus(AssignmentStatus.RETURNED);
        if (request != null && request.notes() != null) {
            assignment.setNotes(request.notes());
        }
        Equipment equipment = assignment.getEquipment();
        if (equipment.getStatus() == EquipmentStatus.ASSIGNED) {
            equipment.setStatus(EquipmentStatus.AVAILABLE);
        }
        return assignment;
    }

    public void delete(Long id) {
        Assignment assignment = findById(id);
        if (assignment.getStatus() == AssignmentStatus.ACTIVE) {
            throw new BusinessRuleException("L'affectation " + id + " est active : clôturez-la d'abord (PATCH /return)");
        }
        assignmentRepository.delete(assignment);
    }
}
