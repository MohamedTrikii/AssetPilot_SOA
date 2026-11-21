package com.assetpilot.asset_service.client;

import com.assetpilot.asset_service.config.MaintenanceClientProperties;
import com.assetpilot.asset_service.dto.MaintenanceDtos.InterventionInfo;
import com.assetpilot.asset_service.dto.MaintenanceDtos.InterventionState;
import com.assetpilot.asset_service.dto.MaintenanceDtos.InterventionStatusEvent;
import com.assetpilot.asset_service.dto.MaintenanceDtos.MaintenanceRequestInfo;
import com.assetpilot.asset_service.exception.RemoteServiceException;
import com.assetpilot.maintenance.proto.v1.CreateMaintenanceRequestRequest;
import com.assetpilot.maintenance.proto.v1.GetInterventionRequest;
import com.assetpilot.maintenance.proto.v1.Intervention;
import com.assetpilot.maintenance.proto.v1.InterventionStatus;
import com.assetpilot.maintenance.proto.v1.MaintenanceRequest;
import com.assetpilot.maintenance.proto.v1.MaintenanceServiceGrpc;
import com.assetpilot.maintenance.proto.v1.ScheduleInterventionRequest;
import com.assetpilot.maintenance.proto.v1.TrackInterventionStatusRequest;
import com.assetpilot.maintenance.proto.v1.UpdateInterventionStatusRequest;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Client gRPC du Maintenance Service. Chaque appel unaire porte un deadline ;
 * les statuts gRPC sont traduits en {@link RemoteServiceException} (statut HTTP équivalent).
 */
@Component
public class MaintenanceGrpcClient {

    public static final String SERVICE_NAME = "maintenance-service";

    @GrpcClient("maintenance")
    private MaintenanceServiceGrpc.MaintenanceServiceBlockingStub blockingStub;

    @GrpcClient("maintenance")
    private MaintenanceServiceGrpc.MaintenanceServiceStub asyncStub;

    private final MaintenanceClientProperties properties;

    public MaintenanceGrpcClient(MaintenanceClientProperties properties) {
        this.properties = properties;
    }

    private MaintenanceServiceGrpc.MaintenanceServiceBlockingStub stub() {
        return blockingStub.withDeadlineAfter(properties.deadline().toMillis(), TimeUnit.MILLISECONDS);
    }

    public MaintenanceRequestInfo createMaintenanceRequest(String assetTag, String description) {
        try {
            MaintenanceRequest created = stub().createMaintenanceRequest(CreateMaintenanceRequestRequest.newBuilder()
                    .setAssetId(assetTag)
                    .setDescription(description)
                    .build()).getMaintenanceRequest();
            return toDto(created);
        } catch (StatusRuntimeException e) {
            throw translate(e);
        }
    }

    public InterventionInfo scheduleIntervention(String maintenanceRequestId, Instant scheduledAt) {
        try {
            Intervention intervention = stub().scheduleIntervention(ScheduleInterventionRequest.newBuilder()
                    .setMaintenanceRequestId(maintenanceRequestId)
                    .setScheduledAt(toTimestamp(scheduledAt))
                    .build()).getIntervention();
            return toDto(intervention);
        } catch (StatusRuntimeException e) {
            throw translate(e);
        }
    }

    public InterventionInfo getIntervention(String interventionId) {
        try {
            return toDto(stub().getIntervention(GetInterventionRequest.newBuilder()
                    .setInterventionId(interventionId)
                    .build()).getIntervention());
        } catch (StatusRuntimeException e) {
            throw translate(e);
        }
    }

    public InterventionInfo updateInterventionStatus(String interventionId, InterventionState status) {
        try {
            return toDto(stub().updateInterventionStatus(UpdateInterventionStatusRequest.newBuilder()
                    .setInterventionId(interventionId)
                    .setStatus(InterventionStatus.valueOf("INTERVENTION_STATUS_" + status.name()))
                    .build()).getIntervention());
        } catch (StatusRuntimeException e) {
            throw translate(e);
        }
    }

    /**
     * Abonnement au flux server-streaming TrackInterventionStatus.
     *
     * @return action permettant d'annuler l'appel gRPC (ex. déconnexion du client HTTP)
     */
    public Runnable trackInterventionStatus(String interventionId,
                                            Consumer<InterventionStatusEvent> onEvent,
                                            Consumer<RemoteServiceException> onError,
                                            Runnable onCompleted) {
        AtomicReference<ClientCallStreamObserver<TrackInterventionStatusRequest>> call = new AtomicReference<>();
        asyncStub.withDeadlineAfter(properties.streamDeadline().toMillis(), TimeUnit.MILLISECONDS)
                .trackInterventionStatus(
                        TrackInterventionStatusRequest.newBuilder().setInterventionId(interventionId).build(),
                        new ClientResponseObserver<TrackInterventionStatusRequest,
                                com.assetpilot.maintenance.proto.v1.InterventionStatusEvent>() {
                            @Override
                            public void beforeStart(ClientCallStreamObserver<TrackInterventionStatusRequest> requestStream) {
                                call.set(requestStream);
                            }

                            @Override
                            public void onNext(com.assetpilot.maintenance.proto.v1.InterventionStatusEvent value) {
                                onEvent.accept(new InterventionStatusEvent(
                                        value.getInterventionId(),
                                        toState(value.getPreviousStatus()),
                                        toState(value.getCurrentStatus()),
                                        toInstant(value.getChangedAt())));
                            }

                            @Override
                            public void onError(Throwable t) {
                                if (Status.fromThrowable(t).getCode() == Status.Code.CANCELLED) {
                                    onCompleted.run();
                                    return;
                                }
                                onError.accept(translate(Status.fromThrowable(t).asRuntimeException()));
                            }

                            @Override
                            public void onCompleted() {
                                onCompleted.run();
                            }
                        });
        return () -> {
            ClientCallStreamObserver<TrackInterventionStatusRequest> observer = call.get();
            if (observer != null) {
                observer.cancel("Client HTTP déconnecté", null);
            }
        };
    }

    // ------------------------------------------------------------------ mapping

    static RemoteServiceException translate(StatusRuntimeException e) {
        Status.Code code = e.getStatus().getCode();
        HttpStatus http = switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case FAILED_PRECONDITION, ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_GATEWAY;
        };
        String description = e.getStatus().getDescription();
        String message = switch (code) {
            case UNAVAILABLE -> "Le Maintenance Service est injoignable";
            case DEADLINE_EXCEEDED -> "Le Maintenance Service n'a pas répondu avant le deadline";
            default -> description != null ? description : code.name();
        };
        return new RemoteServiceException(SERVICE_NAME, http, code.name(), message, e);
    }

    private static MaintenanceRequestInfo toDto(MaintenanceRequest r) {
        return new MaintenanceRequestInfo(r.getId(), r.getAssetId(), r.getDescription(),
                toInstant(r.getCreatedAt()), r.getStatus().name().replace("MAINTENANCE_REQUEST_STATUS_", ""));
    }

    private static InterventionInfo toDto(Intervention i) {
        return new InterventionInfo(i.getId(), i.getMaintenanceRequestId(), i.getAssetId(),
                toInstant(i.getScheduledAt()), toState(i.getStatus()));
    }

    private static InterventionState toState(InterventionStatus status) {
        return switch (status) {
            case INTERVENTION_STATUS_SCHEDULED -> InterventionState.SCHEDULED;
            case INTERVENTION_STATUS_IN_PROGRESS -> InterventionState.IN_PROGRESS;
            case INTERVENTION_STATUS_COMPLETED -> InterventionState.COMPLETED;
            case INTERVENTION_STATUS_CANCELLED -> InterventionState.CANCELLED;
            case INTERVENTION_STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private static Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }

    private static Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
