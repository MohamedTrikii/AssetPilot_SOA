package com.assetpilot.maintenance.grpc;

import com.assetpilot.maintenance.model.InterventionEntity;
import com.assetpilot.maintenance.proto.v1.CreateMaintenanceRequestRequest;
import com.assetpilot.maintenance.proto.v1.CreateMaintenanceRequestResponse;
import com.assetpilot.maintenance.proto.v1.GetInterventionRequest;
import com.assetpilot.maintenance.proto.v1.GetInterventionResponse;
import com.assetpilot.maintenance.proto.v1.InterventionStatusEvent;
import com.assetpilot.maintenance.proto.v1.MaintenanceServiceGrpc;
import com.assetpilot.maintenance.proto.v1.ScheduleInterventionRequest;
import com.assetpilot.maintenance.proto.v1.ScheduleInterventionResponse;
import com.assetpilot.maintenance.proto.v1.TrackInterventionStatusRequest;
import com.assetpilot.maintenance.proto.v1.UpdateInterventionStatusRequest;
import com.assetpilot.maintenance.proto.v1.UpdateInterventionStatusResponse;
import com.assetpilot.maintenance.service.InterventionStatusChange;
import com.assetpilot.maintenance.service.InterventionStatusPublisher;
import com.assetpilot.maintenance.service.MaintenanceBusinessService;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implémentation du service gRPC {@code assetpilot.maintenance.v1.MaintenanceService}.
 * Les exceptions métier sont converties en statuts gRPC par {@link GrpcExceptionAdvice}.
 */
@GrpcService
public class MaintenanceGrpcService extends MaintenanceServiceGrpc.MaintenanceServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceGrpcService.class);

    private final MaintenanceBusinessService service;
    private final InterventionStatusPublisher publisher;

    public MaintenanceGrpcService(MaintenanceBusinessService service, InterventionStatusPublisher publisher) {
        this.service = service;
        this.publisher = publisher;
    }

    @Override
    public void createMaintenanceRequest(CreateMaintenanceRequestRequest request,
                                         StreamObserver<CreateMaintenanceRequestResponse> responseObserver) {
        var created = service.createRequest(request.getAssetId(), request.getDescription());
        responseObserver.onNext(CreateMaintenanceRequestResponse.newBuilder()
                .setMaintenanceRequest(ProtoMapper.toProto(created))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void scheduleIntervention(ScheduleInterventionRequest request,
                                     StreamObserver<ScheduleInterventionResponse> responseObserver) {
        Instant scheduledAt = request.hasScheduledAt() ? ProtoMapper.toInstant(request.getScheduledAt()) : null;
        var intervention = service.scheduleIntervention(request.getMaintenanceRequestId(), scheduledAt);
        responseObserver.onNext(ScheduleInterventionResponse.newBuilder()
                .setIntervention(ProtoMapper.toProto(intervention))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getIntervention(GetInterventionRequest request,
                                StreamObserver<GetInterventionResponse> responseObserver) {
        var intervention = service.getIntervention(request.getInterventionId());
        responseObserver.onNext(GetInterventionResponse.newBuilder()
                .setIntervention(ProtoMapper.toProto(intervention))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateInterventionStatus(UpdateInterventionStatusRequest request,
                                         StreamObserver<UpdateInterventionStatusResponse> responseObserver) {
        var intervention = service.updateInterventionStatus(
                request.getInterventionId(), ProtoMapper.fromProto(request.getStatus()));
        responseObserver.onNext(UpdateInterventionStatusResponse.newBuilder()
                .setIntervention(ProtoMapper.toProto(intervention))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * Server streaming : émet d'abord le statut courant, puis un événement à chaque changement.
     * Le flux se termine quand l'intervention atteint un statut terminal (COMPLETED / CANCELLED),
     * ou quand le client annule l'appel / dépasse son deadline.
     */
    @Override
    public void trackInterventionStatus(TrackInterventionStatusRequest request,
                                        StreamObserver<InterventionStatusEvent> responseObserver) {
        String interventionId = request.getInterventionId();
        // Valide l'existence (NOT_FOUND / INVALID_ARGUMENT levés avant toute émission)
        service.getIntervention(interventionId);

        ServerCallStreamObserver<InterventionStatusEvent> stream =
                (ServerCallStreamObserver<InterventionStatusEvent>) responseObserver;
        Object lock = new Object();
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicReference<Runnable> unsubscribe = new AtomicReference<>(() -> { });

        stream.setOnCancelHandler(() -> {
            closed.set(true);
            unsubscribe.get().run();
            log.info("Suivi de l'intervention {} annulé par le client", interventionId);
        });

        java.util.function.Consumer<InterventionStatusChange> emitter = change -> {
            synchronized (lock) {
                if (closed.get() || stream.isCancelled()) {
                    return;
                }
                stream.onNext(ProtoMapper.toProto(change));
                if (change.currentStatus().isTerminal()) {
                    closed.set(true);
                    unsubscribe.get().run();
                    stream.onCompleted();
                }
            }
        };

        // Abonnement avant la lecture de l'état courant : aucun changement ne peut être manqué
        unsubscribe.set(publisher.subscribe(interventionId, emitter));

        InterventionEntity current = service.getIntervention(interventionId);
        emitter.accept(new InterventionStatusChange(interventionId, null, current.getStatus(), Instant.now()));
    }
}
