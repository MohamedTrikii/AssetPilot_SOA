package com.assetpilot.asset_service.config;

import com.assetpilot.asset_service.domain.EquipmentType;
import com.assetpilot.asset_service.dto.AssignmentDtos.AssignmentRequest;
import com.assetpilot.asset_service.dto.EmployeeDtos.EmployeeRequest;
import com.assetpilot.asset_service.dto.EquipmentDtos.EquipmentRequest;
import com.assetpilot.asset_service.repository.EquipmentRepository;
import com.assetpilot.asset_service.service.AssignmentService;
import com.assetpilot.asset_service.service.EmployeeService;
import com.assetpilot.asset_service.service.EquipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Jeu de données de démonstration. Les assetTag AST-001..AST-004 correspondent aux garanties
 * pré-chargées dans le Warranty Service ; AST-005 n'a pas de garantie (SOAP Fault WARRANTY_NOT_FOUND).
 */
@Component
@ConditionalOnProperty(name = "assetpilot.seed-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EquipmentRepository equipmentRepository;
    private final EquipmentService equipmentService;
    private final EmployeeService employeeService;
    private final AssignmentService assignmentService;

    public DataInitializer(EquipmentRepository equipmentRepository, EquipmentService equipmentService,
                           EmployeeService employeeService, AssignmentService assignmentService) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentService = equipmentService;
        this.employeeService = employeeService;
        this.assignmentService = assignmentService;
    }

    @Override
    public void run(String... args) {
        if (equipmentRepository.count() > 0) {
            return;
        }
        var e1 = equipmentService.create(new EquipmentRequest("AST-001", "Laptop Dell Latitude 7440", EquipmentType.LAPTOP,
                "Dell", "Latitude 7440", "SN-DL-7440-001", LocalDate.now().minusMonths(6), null));
        equipmentService.create(new EquipmentRequest("AST-002", "PC HP EliteDesk 800", EquipmentType.DESKTOP,
                "HP", "EliteDesk 800 G6", "SN-HP-800-002", LocalDate.now().minusYears(3), null));
        equipmentService.create(new EquipmentRequest("AST-003", "MacBook Pro 14", EquipmentType.LAPTOP,
                "Apple", "MacBook Pro M3", "SN-AP-MBP-003", LocalDate.now().minusMonths(10), null));
        equipmentService.create(new EquipmentRequest("AST-004", "Lenovo ThinkPad T14", EquipmentType.LAPTOP,
                "Lenovo", "ThinkPad T14 Gen 4", "SN-LN-T14-004", LocalDate.now().minusYears(1), null));
        equipmentService.create(new EquipmentRequest("AST-005", "Imprimante Canon i-SENSYS", EquipmentType.PRINTER,
                "Canon", "i-SENSYS MF455", "SN-CN-455-005", LocalDate.now().minusYears(2), null));

        var emp1 = employeeService.create(new EmployeeRequest("Amine", "Gharbi", "amine.gharbi@assetpilot.tn",
                "Informatique", "Administrateur systèmes"));
        employeeService.create(new EmployeeRequest("Sarra", "Ben Salah", "sarra.bensalah@assetpilot.tn",
                "Finance", "Comptable"));
        employeeService.create(new EmployeeRequest("Youssef", "Mansour", "youssef.mansour@assetpilot.tn",
                "Ressources humaines", "Chargé RH"));

        assignmentService.create(new AssignmentRequest(e1.getId(), emp1.getId(), LocalDate.now().minusMonths(5),
                "Poste de travail principal"));
        log.info("Jeu de données de démonstration chargé (5 équipements, 3 employés, 1 affectation)");
    }
}
