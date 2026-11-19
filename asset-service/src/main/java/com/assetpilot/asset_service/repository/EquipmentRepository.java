package com.assetpilot.asset_service.repository;

import com.assetpilot.asset_service.domain.Equipment;
import com.assetpilot.asset_service.domain.EquipmentStatus;
import com.assetpilot.asset_service.domain.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findByAssetTag(String assetTag);

    boolean existsByAssetTag(String assetTag);

    boolean existsBySerialNumber(String serialNumber);

    @Query("""
            select e from Equipment e
            where (:status is null or e.status = :status)
              and (:type is null or e.type = :type)
            order by e.id
            """)
    List<Equipment> search(@Param("status") EquipmentStatus status, @Param("type") EquipmentType type);
}
