package com.assetpilot.maintenance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "interventions")
public class InterventionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String maintenanceRequestId;

    @Column(nullable = false, length = 64)
    private String assetId;

    @Column(nullable = false)
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterventionStatus status;

    protected InterventionEntity() {
    }

    public InterventionEntity(String id, String maintenanceRequestId, String assetId, Instant scheduledAt,
                              InterventionStatus status) {
        this.id = id;
        this.maintenanceRequestId = maintenanceRequestId;
        this.assetId = assetId;
        this.scheduledAt = scheduledAt;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getMaintenanceRequestId() {
        return maintenanceRequestId;
    }

    public String getAssetId() {
        return assetId;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public InterventionStatus getStatus() {
        return status;
    }

    public void setStatus(InterventionStatus status) {
        this.status = status;
    }
}
