package com.assetpilot.warranty.repository;

import com.assetpilot.warranty.model.WarrantyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarrantyRepository extends JpaRepository<WarrantyEntity, String> {

    Optional<WarrantyEntity> findByAssetId(String assetId);

    Optional<WarrantyEntity> findByContractNumber(String contractNumber);

    boolean existsByAssetId(String assetId);
}
