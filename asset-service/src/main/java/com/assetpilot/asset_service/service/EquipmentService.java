package com.assetpilot.asset_service.service;

import com.assetpilot.asset_service.domain.AssignmentStatus;
import com.assetpilot.asset_service.domain.Equipment;
import com.assetpilot.asset_service.domain.EquipmentStatus;
import com.assetpilot.asset_service.domain.EquipmentType;
import com.assetpilot.asset_service.dto.EquipmentDtos.EquipmentRequest;
import com.assetpilot.asset_service.exception.BusinessRuleException;
import com.assetpilot.asset_service.exception.ResourceNotFoundException;
import com.assetpilot.asset_service.repository.AssignmentRepository;
import com.assetpilot.asset_service.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class EquipmentService {

    /** Statuts qu'un client peut positionner directement ; les autres découlent des règles métier. */
    private static final EnumSet<EquipmentStatus> MANUAL_STATUSES = EnumSet.of(EquipmentStatus.AVAILABLE, EquipmentStatus.RETIRED);

    private final EquipmentRepository equipmentRepository;
    private final AssignmentRepository assignmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, AssignmentRepository assignmentRepository) {
        this.equipmentRepository = equipmentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Equipment> findAll(EquipmentStatus status, EquipmentType type) {
        return equipmentRepository.search(status, type);
    }

    @Transactional(readOnly = true)
    public Equipment findById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement", id));
    }

    public Equipment create(EquipmentRequest request) {
        String assetTag = request.assetTag().trim();
        String serial = request.serialNumber().trim();
        if (equipmentRepository.existsByAssetTag(assetTag)) {
            throw new BusinessRuleException("Un équipement avec l'assetTag " + assetTag + " existe déjà");
        }
        if (equipmentRepository.existsBySerialNumber(serial)) {
            throw new BusinessRuleException("Un équipement avec le numéro de série " + serial + " existe déjà");
        }
        Equipment equipment = new Equipment();
        apply(equipment, request);
        EquipmentStatus initial = request.status() == null ? EquipmentStatus.AVAILABLE : request.status();
        checkManualStatus(initial);
        equipment.setStatus(initial);
        return equipmentRepository.save(equipment);
    }

    public Equipment update(Long id, EquipmentRequest request) {
        Equipment equipment = findById(id);
        String assetTag = request.assetTag().trim();
        String serial = request.serialNumber().trim();
        if (!equipment.getAssetTag().equals(assetTag) && equipmentRepository.existsByAssetTag(assetTag)) {
            throw new BusinessRuleException("Un équipement avec l'assetTag " + assetTag + " existe déjà");
        }
        if (!equipment.getSerialNumber().equals(serial) && equipmentRepository.existsBySerialNumber(serial)) {
            throw new BusinessRuleException("Un équipement avec le numéro de série " + serial + " existe déjà");
        }
        apply(equipment, request);

        if (request.status() != null && !Objects.equals(request.status(), equipment.getStatus())) {
            checkManualStatus(request.status());
            if (equipment.getStatus() == EquipmentStatus.ASSIGNED || equipment.getStatus() == EquipmentStatus.IN_MAINTENANCE) {
                throw new BusinessRuleException("Impossible de changer le statut d'un équipement "
                        + equipment.getStatus() + " : terminez d'abord l'affectation ou la maintenance");
            }
            equipment.setStatus(request.status());
        }
        return equipment;
    }

    public void delete(Long id) {
        Equipment equipment = findById(id);
        if (assignmentRepository.existsByEquipmentId(id)) {
            throw new BusinessRuleException("L'équipement " + equipment.getAssetTag()
                    + " possède un historique d'affectations : passez-le au statut RETIRED plutôt que de le supprimer");
        }
        if (equipment.getStatus() == EquipmentStatus.IN_MAINTENANCE) {
            throw new BusinessRuleException("L'équipement " + equipment.getAssetTag() + " est en maintenance");
        }
        equipmentRepository.delete(equipment);
    }

    /** Passe l'équipement en maintenance (appelé après création de la demande gRPC). */
    public Equipment markInMaintenance(Long id) {
        Equipment equipment = findById(id);
        equipment.setStatus(EquipmentStatus.IN_MAINTENANCE);
        return equipment;
    }

    /** Fin de maintenance : l'équipement redevient ASSIGNED s'il a une affectation active, sinon AVAILABLE. */
    public void releaseFromMaintenance(String assetTag) {
        equipmentRepository.findByAssetTag(assetTag).ifPresent(equipment -> {
            if (equipment.getStatus() != EquipmentStatus.IN_MAINTENANCE) {
                return;
            }
            boolean assigned = assignmentRepository.existsByEquipmentIdAndStatus(equipment.getId(), AssignmentStatus.ACTIVE);
            equipment.setStatus(assigned ? EquipmentStatus.ASSIGNED : EquipmentStatus.AVAILABLE);
        });
    }

    private static void checkManualStatus(EquipmentStatus status) {
        if (!MANUAL_STATUSES.contains(status)) {
            throw new BusinessRuleException("Le statut " + status
                    + " est géré automatiquement (affectations / maintenance) ; valeurs autorisées : " + MANUAL_STATUSES);
        }
    }

    private static void apply(Equipment equipment, EquipmentRequest request) {
        equipment.setAssetTag(request.assetTag().trim());
        equipment.setName(request.name().trim());
        equipment.setType(request.type());
        equipment.setBrand(request.brand());
        equipment.setModel(request.model());
        equipment.setSerialNumber(request.serialNumber().trim());
        equipment.setPurchaseDate(request.purchaseDate());
    }
}
