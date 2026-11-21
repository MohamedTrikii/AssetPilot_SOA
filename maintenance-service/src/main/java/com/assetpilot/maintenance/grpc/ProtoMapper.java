package com.assetpilot.maintenance.grpc;

import com.assetpilot.maintenance.model.InterventionEntity;
import com.assetpilot.maintenance.model.MaintenanceRequestEntity;
import com.assetpilot.maintenance.proto.v1.Intervention;
import com.assetpilot.maintenance.proto.v1.InterventionStatus;
import com.assetpilot.maintenance.proto.v1.InterventionStatusEvent;
import com.assetpilot.maintenance.proto.v1.MaintenanceRequest;
import com.assetpilot.maintenance.proto.v1.MaintenanceRequestStatus;
import com.assetpilot.maintenance.service.InterventionStatusChange;
import com.google.protobuf.Timestamp;

import java.time.Instant;

/**
 * Conversion entre le modèle métier et les messages Protocol Buffers.
 */
final class ProtoMapper {

    private ProtoMapper() {
    }

    static MaintenanceRequest toProto(MaintenanceRequestEntity entity) {
        return MaintenanceRequest.newBuilder()
                .setId(entity.getId())
                .setAssetId(entity.getAssetId())
                .setDescription(entity.getDescription())
                .setCreatedAt(toTimestamp(entity.getCreatedAt()))
                .setStatus(toProto(entity.getStatus()))
                .build();
    }

    static Intervention toProto(InterventionEntity entity) {
        return Intervention.newBuilder()
                .setId(entity.getId())
                .setMaintenanceRequestId(entity.getMaintenanceRequestId())
                .setAssetId(entity.getAssetId())
                .setScheduledAt(toTimestamp(entity.getScheduledAt()))
                .setStatus(toProto(entity.getStatus()))
                .build();
    }

    static InterventionStatusEvent toProto(InterventionStatusChange change) {
        return InterventionStatusEvent.newBuilder()
                .setInterventionId(change.interventionId())
                .setPreviousStatus(change.previousStatus() == null
                        ? InterventionStatus.INTERVENTION_STATUS_UNSPECIFIED
                        : toProto(change.previousStatus()))
                .setCurrentStatus(toProto(change.currentStatus()))
                .setChangedAt(toTimestamp(change.changedAt()))
                .build();
    }

    static MaintenanceRequestStatus toProto(com.assetpilot.maintenance.model.MaintenanceRequestStatus status) {
        return switch (status) {
            case OPEN -> MaintenanceRequestStatus.MAINTENANCE_REQUEST_STATUS_OPEN;
            case SCHEDULED -> MaintenanceRequestStatus.MAINTENANCE_REQUEST_STATUS_SCHEDULED;
            case RESOLVED -> MaintenanceRequestStatus.MAINTENANCE_REQUEST_STATUS_RESOLVED;
            case CANCELLED -> MaintenanceRequestStatus.MAINTENANCE_REQUEST_STATUS_CANCELLED;
        };
    }

    static InterventionStatus toProto(com.assetpilot.maintenance.model.InterventionStatus status) {
        return switch (status) {
            case SCHEDULED -> InterventionStatus.INTERVENTION_STATUS_SCHEDULED;
            case IN_PROGRESS -> InterventionStatus.INTERVENTION_STATUS_IN_PROGRESS;
            case COMPLETED -> InterventionStatus.INTERVENTION_STATUS_COMPLETED;
            case CANCELLED -> InterventionStatus.INTERVENTION_STATUS_CANCELLED;
        };
    }

    /** @return le statut métier, ou null si non spécifié / inconnu. */
    static com.assetpilot.maintenance.model.InterventionStatus fromProto(InterventionStatus status) {
        return switch (status) {
            case INTERVENTION_STATUS_SCHEDULED -> com.assetpilot.maintenance.model.InterventionStatus.SCHEDULED;
            case INTERVENTION_STATUS_IN_PROGRESS -> com.assetpilot.maintenance.model.InterventionStatus.IN_PROGRESS;
            case INTERVENTION_STATUS_COMPLETED -> com.assetpilot.maintenance.model.InterventionStatus.COMPLETED;
            case INTERVENTION_STATUS_CANCELLED -> com.assetpilot.maintenance.model.InterventionStatus.CANCELLED;
            case INTERVENTION_STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    static Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    static Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
