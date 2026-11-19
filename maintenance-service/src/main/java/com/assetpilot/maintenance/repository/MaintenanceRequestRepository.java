package com.assetpilot.maintenance.repository;

import com.assetpilot.maintenance.model.MaintenanceRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequestEntity, String> {
}
