package com.assetpilot.asset_service.repository;

import com.assetpilot.asset_service.domain.Assignment;
import com.assetpilot.asset_service.domain.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    boolean existsByEquipmentIdAndStatus(Long equipmentId, AssignmentStatus status);

    boolean existsByEquipmentId(Long equipmentId);

    boolean existsByEmployeeId(Long employeeId);

    @Query("""
            select a from Assignment a
            where (:employeeId is null or a.employee.id = :employeeId)
              and (:equipmentId is null or a.equipment.id = :equipmentId)
              and (:status is null or a.status = :status)
            order by a.id
            """)
    List<Assignment> search(@Param("employeeId") Long employeeId,
                            @Param("equipmentId") Long equipmentId,
                            @Param("status") AssignmentStatus status);
}
