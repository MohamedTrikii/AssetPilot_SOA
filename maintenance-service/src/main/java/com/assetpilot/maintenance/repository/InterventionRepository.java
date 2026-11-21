package com.assetpilot.maintenance.repository;

import com.assetpilot.maintenance.model.InterventionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventionRepository extends JpaRepository<InterventionEntity, String> {
}
