package com.assetpilot.asset_service.service;

import com.assetpilot.asset_service.domain.Employee;
import com.assetpilot.asset_service.dto.EmployeeDtos.EmployeeRequest;
import com.assetpilot.asset_service.exception.BusinessRuleException;
import com.assetpilot.asset_service.exception.ResourceNotFoundException;
import com.assetpilot.asset_service.repository.AssignmentRepository;
import com.assetpilot.asset_service.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AssignmentRepository assignmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository, AssignmentRepository assignmentRepository) {
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé", id));
    }

    public Employee create(EmployeeRequest request) {
        String email = request.email().trim();
        if (employeeRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Un employé avec l'email " + email + " existe déjà");
        }
        Employee employee = new Employee();
        apply(employee, request);
        return employeeRepository.save(employee);
    }

    public Employee update(Long id, EmployeeRequest request) {
        Employee employee = findById(id);
        String email = request.email().trim();
        if (!employee.getEmail().equalsIgnoreCase(email) && employeeRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Un employé avec l'email " + email + " existe déjà");
        }
        apply(employee, request);
        return employee;
    }

    public void delete(Long id) {
        Employee employee = findById(id);
        if (assignmentRepository.existsByEmployeeId(id)) {
            throw new BusinessRuleException("L'employé " + employee.getFullName()
                    + " possède des affectations : impossible de le supprimer");
        }
        employeeRepository.delete(employee);
    }

    private static void apply(Employee employee, EmployeeRequest request) {
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setEmail(request.email().trim());
        employee.setDepartment(request.department());
        employee.setJobTitle(request.jobTitle());
    }
}
