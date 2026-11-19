package com.assetpilot.maintenance.grpc;

import com.assetpilot.maintenance.proto.v1.CreateMaintenanceRequestRequest;
import com.assetpilot.maintenance.proto.v1.GetInterventionRequest;
import com.assetpilot.maintenance.proto.v1.Intervention;
import com.assetpilot.maintenance.proto.v1.InterventionStatus;
import com.assetpilot.maintenance.proto.v1.InterventionStatusEvent;
import com.assetpilot.maintenance.proto.v1.MaintenanceRequest;
import com.assetpilot.maintenance.proto.v1.MaintenanceRequestStatus;
import com.assetpilot.maintenance.proto.v1.MaintenanceServiceGrpc;
import com.assetpilot.maintenance.proto.v1.ScheduleInterventionRequest;
import com.assetpilot.maintenance.proto.v1.TrackInterventionStatusRequest;
import com.assetpilot.maintenance.proto.v1.UpdateInterventionStatusRequest;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test d'intégration gRPC : contexte Spring complet + serveur gRPC en mémoire.
 */
@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=maintenance-test",
        "server.port=0"
})
class MaintenanceGrpcIntegrationTest {

    private ManagedChannel channel;
    private MaintenanceServiceGrpc.MaintenanceServiceBlockingStub blockingStub;
    private MaintenanceServiceGrpc.MaintenanceServiceStub asyncStub;

    @BeforeEach
    void setUp() {
        channel = InProcessChannelBuilder.forName("maintenance-test").build();
        blockingStub = MaintenanceServiceGrpc.newBlockingStub(channel);
        asyncStub = MaintenanceServiceGrpc.newStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
    }

    private MaintenanceRequest createRequest() {
        return blockingStub.withDeadlineAfter(2, TimeUnit.SECONDS)
                .createMaintenanceRequest(CreateMaintenanceRequestRequest.newBuilder()
                        .setAssetId("AST-001")
                        .setDescription("Écran qui clignote")
                        .build())
                .getMaintenanceRequest();
    }

    private Intervention schedule(String requestId) {
        Instant tomorrow = Instant.now().plusSeconds(86_400);
        return blockingStub.withDeadlineAfter(2, TimeUnit.SECONDS)
                .scheduleIntervention(ScheduleInterventionRequest.newBuilder()
                        .setMaintenanceRequestId(requestId)
                        .setScheduledAt(ProtoMapper.toTimestamp(tomorrow))
                        .build())
                .getIntervention();
    }

    private Intervention updateStatus(String interventionId, InterventionStatus status) {
        return blockingStub.withDeadlineAfter(2, TimeUnit.SECONDS)
                .updateInterventionStatus(UpdateInterventionStatusRequest.newBuilder()
                        .setInterventionId(interventionId)
                        .setStatus(status)
                        .build())
                .getIntervention();
    }

    @Test
    @DisplayName("Unaire : création puis planification d'une intervention")
    void createAndSchedule() {
        MaintenanceRequest request = createRequest();
        assertThat(request.getId()).isNotBlank();
        assertThat(request.getStatus()).isEqualTo(MaintenanceRequestStatus.MAINTENANCE_REQUEST_STATUS_OPEN);

        Intervention intervention = schedule(request.getId());
        assertThat(intervention.getMaintenanceRequestId()).isEqualTo(request.getId());
        assertThat(intervention.getAssetId()).isEqualTo("AST-001");
        assertThat(intervention.getStatus()).isEqualTo(InterventionStatus.INTERVENTION_STATUS_SCHEDULED);

        Intervention fetched = blockingStub.getIntervention(GetInterventionRequest.newBuilder()
                .setInterventionId(intervention.getId()).build()).getIntervention();
        assertThat(fetched).isEqualTo(intervention);
    }

    @Test
    @DisplayName("Statuts gRPC : INVALID_ARGUMENT, NOT_FOUND, FAILED_PRECONDITION")
    void errorStatuses() {
        assertThatThrownBy(() -> blockingStub.createMaintenanceRequest(
                CreateMaintenanceRequestRequest.newBuilder().setAssetId("").setDescription("x").build()))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(e -> ((StatusRuntimeException) e).getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);

        assertThatThrownBy(() -> blockingStub.getIntervention(
                GetInterventionRequest.newBuilder().setInterventionId("inconnu").build()))
                .extracting(e -> ((StatusRuntimeException) e).getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);

        Intervention intervention = schedule(createRequest().getId());
        // SCHEDULED -> COMPLETED est interdit (il faut passer par IN_PROGRESS)
        assertThatThrownBy(() -> updateStatus(intervention.getId(), InterventionStatus.INTERVENTION_STATUS_COMPLETED))
                .extracting(e -> ((StatusRuntimeException) e).getStatus().getCode())
                .isEqualTo(Status.Code.FAILED_PRECONDITION);
    }

    @Test
    @DisplayName("Server streaming : suivi des changements de statut jusqu'à COMPLETED")
    void trackStatusStream() throws InterruptedException {
        Intervention intervention = schedule(createRequest().getId());

        List<InterventionStatusEvent> events = new CopyOnWriteArrayList<>();
        CountDownLatch firstEvent = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(1);

        asyncStub.withDeadlineAfter(10, TimeUnit.SECONDS).trackInterventionStatus(
                TrackInterventionStatusRequest.newBuilder().setInterventionId(intervention.getId()).build(),
                new StreamObserver<>() {
                    @Override
                    public void onNext(InterventionStatusEvent value) {
                        events.add(value);
                        firstEvent.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        completed.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        completed.countDown();
                    }
                });

        assertThat(firstEvent.await(5, TimeUnit.SECONDS)).isTrue();
        updateStatus(intervention.getId(), InterventionStatus.INTERVENTION_STATUS_IN_PROGRESS);
        updateStatus(intervention.getId(), InterventionStatus.INTERVENTION_STATUS_COMPLETED);

        assertThat(completed.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(events).extracting(InterventionStatusEvent::getCurrentStatus).containsExactly(
                InterventionStatus.INTERVENTION_STATUS_SCHEDULED,
                InterventionStatus.INTERVENTION_STATUS_IN_PROGRESS,
                InterventionStatus.INTERVENTION_STATUS_COMPLETED);
        assertThat(events.get(1).getPreviousStatus()).isEqualTo(InterventionStatus.INTERVENTION_STATUS_SCHEDULED);
    }

    @Test
    @DisplayName("Deadline : le flux est interrompu avec DEADLINE_EXCEEDED")
    void streamDeadlineExceeded() throws InterruptedException {
        Intervention intervention = schedule(createRequest().getId());
        CountDownLatch done = new CountDownLatch(1);
        List<Status.Code> codes = new CopyOnWriteArrayList<>();

        asyncStub.withDeadlineAfter(300, TimeUnit.MILLISECONDS).trackInterventionStatus(
                TrackInterventionStatusRequest.newBuilder().setInterventionId(intervention.getId()).build(),
                new StreamObserver<>() {
                    @Override
                    public void onNext(InterventionStatusEvent value) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        codes.add(Status.fromThrowable(t).getCode());
                        done.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        done.countDown();
                    }
                });

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(codes).containsExactly(Status.Code.DEADLINE_EXCEEDED);
    }
}
