package com.assetpilot.asset_service.integration;

import com.assetpilot.maintenance.proto.v1.CreateMaintenanceRequestRequest;
import com.assetpilot.maintenance.proto.v1.CreateMaintenanceRequestResponse;
import com.assetpilot.maintenance.proto.v1.Intervention;
import com.assetpilot.maintenance.proto.v1.InterventionStatus;
import com.assetpilot.maintenance.proto.v1.MaintenanceRequest;
import com.assetpilot.maintenance.proto.v1.MaintenanceRequestStatus;
import com.assetpilot.maintenance.proto.v1.MaintenanceServiceGrpc;
import com.assetpilot.maintenance.proto.v1.ScheduleInterventionRequest;
import com.assetpilot.maintenance.proto.v1.ScheduleInterventionResponse;
import com.assetpilot.maintenance.proto.v1.UpdateInterventionStatusRequest;
import com.assetpilot.maintenance.proto.v1.UpdateInterventionStatusResponse;
import com.google.protobuf.Timestamp;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.ws.test.client.RequestMatchers.payload;
import static org.springframework.ws.test.client.ResponseCreators.withClientOrSenderFault;
import static org.springframework.ws.test.client.ResponseCreators.withException;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

/**
 * Test du flux Asset → Warranty (SOAP, MockWebServiceServer) → Maintenance (gRPC, serveur in-process).
 * Les données de démonstration sont chargées : AST-001 a l'id 1.
 */
@SpringBootTest(properties = "grpc.client.maintenance.address=in-process:asset-flow-test")
@AutoConfigureMockMvc
@DirtiesContext
class InterServiceFlowTest {

    private static Server grpcServer;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebServiceTemplate warrantyWebServiceTemplate;

    private MockWebServiceServer soapServer;

    /** Faux Maintenance Service : renvoie des réponses déterministes. */
    static class FakeMaintenance extends MaintenanceServiceGrpc.MaintenanceServiceImplBase {
        @Override
        public void createMaintenanceRequest(CreateMaintenanceRequestRequest req,
                                             StreamObserver<CreateMaintenanceRequestResponse> obs) {
            obs.onNext(CreateMaintenanceRequestResponse.newBuilder().setMaintenanceRequest(MaintenanceRequest.newBuilder()
                    .setId("MR-1").setAssetId(req.getAssetId()).setDescription(req.getDescription())
                    .setCreatedAt(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()))
                    .setStatus(MaintenanceRequestStatus.MAINTENANCE_REQUEST_STATUS_OPEN)).build());
            obs.onCompleted();
        }

        @Override
        public void scheduleIntervention(ScheduleInterventionRequest req, StreamObserver<ScheduleInterventionResponse> obs) {
            obs.onNext(ScheduleInterventionResponse.newBuilder().setIntervention(intervention(
                    InterventionStatus.INTERVENTION_STATUS_SCHEDULED, req.getScheduledAt())).build());
            obs.onCompleted();
        }

        @Override
        public void updateInterventionStatus(UpdateInterventionStatusRequest req,
                                             StreamObserver<UpdateInterventionStatusResponse> obs) {
            if (!"INT-1".equals(req.getInterventionId())) {
                obs.onError(Status.NOT_FOUND.withDescription("Intervention introuvable").asRuntimeException());
                return;
            }
            obs.onNext(UpdateInterventionStatusResponse.newBuilder()
                    .setIntervention(intervention(req.getStatus(), Timestamp.getDefaultInstance())).build());
            obs.onCompleted();
        }

        private static Intervention intervention(InterventionStatus status, Timestamp at) {
            return Intervention.newBuilder().setId("INT-1").setMaintenanceRequestId("MR-1")
                    .setAssetId("AST-001").setScheduledAt(at).setStatus(status).build();
        }
    }

    @BeforeAll
    static void startGrpc() throws IOException {
        grpcServer = InProcessServerBuilder.forName("asset-flow-test").directExecutor()
                .addService(new FakeMaintenance()).build().start();
    }

    @AfterAll
    static void stopGrpc() {
        grpcServer.shutdownNow();
    }

    @BeforeEach
    void setUp() {
        soapServer = MockWebServiceServer.createServer(warrantyWebServiceTemplate);
    }

    private static final String COVERED = """
            <CheckWarrantyResponse xmlns="http://assetpilot.com/warranty/v1">
              <assetId>AST-001</assetId><isCovered>true</isCovered><status>ACTIVE</status>
              <expiryDate>2028-11-15</expiryDate><coverageType>PREMIUM</coverageType>
              <message>Équipement sous garantie</message>
            </CheckWarrantyResponse>""";

    @Test
    @DisplayName("SOAP CheckWarranty puis gRPC Create + Schedule : ticket complet, équipement IN_MAINTENANCE")
    void openMaintenanceFullFlow() throws Exception {
        soapServer.expect(payload(new StringSource(
                        "<CheckWarrantyRequest xmlns=\"http://assetpilot.com/warranty/v1\"><assetId>AST-001</assetId></CheckWarrantyRequest>")))
                .andRespond(withPayload(new StringSource(COVERED)));

        mvc.perform(post("/api/v1/equipments/1/maintenance-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Écran noir\",\"scheduledAt\":\"" + Instant.now().plusSeconds(86400) + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warranty.covered").value(true))
                .andExpect(jsonPath("$.warranty.coverageType").value("PREMIUM"))
                .andExpect(jsonPath("$.maintenanceRequest.id").value("MR-1"))
                .andExpect(jsonPath("$.maintenanceRequest.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.intervention.status").value("SCHEDULED"));
        soapServer.verify();

        mvc.perform(get("/api/v1/equipments/1")).andExpect(jsonPath("$.status").value("IN_MAINTENANCE"));

        // Fin d'intervention : l'équipement (affecté) redevient ASSIGNED
        mvc.perform(patch("/api/v1/interventions/INT-1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(get("/api/v1/equipments/1")).andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("SOAP Fault WARRANTY_NOT_FOUND : covered=false, la demande est tout de même créée")
    void soapFaultNotFound() throws Exception {
        soapServer.expect(payload(new StringSource(
                        "<CheckWarrantyRequest xmlns=\"http://assetpilot.com/warranty/v1\"><assetId>AST-005</assetId></CheckWarrantyRequest>")))
                .andRespond(withClientOrSenderFault("WARRANTY_NOT_FOUND", java.util.Locale.ENGLISH));

        mvc.perform(post("/api/v1/equipments/5/maintenance-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Bourrage papier\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warranty.covered").value(false))
                .andExpect(jsonPath("$.warranty.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.intervention").doesNotExist());
    }

    @Test
    @DisplayName("Warranty Service indisponible : repli UNKNOWN sur le flux, 503 sur l'appel direct")
    void warrantyUnavailable() throws Exception {
        soapServer.expect(payload(new StringSource(
                        "<CheckWarrantyRequest xmlns=\"http://assetpilot.com/warranty/v1\"><assetId>AST-003</assetId></CheckWarrantyRequest>")))
                .andRespond(withException(new IOException("Connection refused")));
        mvc.perform(post("/api/v1/equipments/3/maintenance-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Clavier HS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warranty.status").value("UNKNOWN"))
                .andExpect(jsonPath("$.warranty.verified").value(false));

        soapServer.reset();
        soapServer.expect(payload(new StringSource(
                        "<CheckWarrantyRequest xmlns=\"http://assetpilot.com/warranty/v1\"><assetId>AST-002</assetId></CheckWarrantyRequest>")))
                .andRespond(withException(new IOException("Connection refused")));
        mvc.perform(get("/api/v1/equipments/2/warranty"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.service").value("warranty-service"));
    }

    @Test
    @DisplayName("Statut gRPC NOT_FOUND traduit en HTTP 404")
    void grpcNotFound() throws Exception {
        mvc.perform(patch("/api/v1/interventions/INCONNU/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.remoteCode").value("NOT_FOUND"));
    }
}
