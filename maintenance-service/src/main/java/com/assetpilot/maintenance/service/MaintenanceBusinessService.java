package com.assetpilot.maintenance.service;

import com.assetpilot.maintenance.model.InterventionEntity;
import com.assetpilot.maintenance.model.InterventionStatus;
import com.assetpilot.maintenance.model.MaintenanceRequestEntity;
import com.assetpilot.maintenance.model.MaintenanceRequestStatus;
import com.assetpilot.maintenance.repository.InterventionRepository;
import com.assetpilot.maintenance.repository.MaintenanceRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Logique métier : création des demandes, planification et transitions de statut des interventions.
 */
@Service
public class MaintenanceBusinessService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceBusinessService.class);

    /** Tolérance d'horloge entre services pour la date de planification. */
    private static final Duration CLOCK_SKEW = Duration.ofMinutes(1);

    private final MaintenanceRequestRepository requestRepository;
    private final InterventionRepository interventionRepository;
    private final InterventionStatusPublisher publisher;

    public MaintenanceBusinessService(MaintenanceRequestRepository requestRepository,
                                      InterventionRepository interventionRepository,
                                      InterventionStatusPublisher publisher) {
        this.requestRepository = requestRepository;
        this.interventionRepository = interventionRepository;
        this.publisher = publisher;
    }

    @Transactional
    public MaintenanceRequestEntity createRequest(String assetId, String description) {
        if (isBlank(assetId)) {
            throw MaintenanceException.invalidArgument("asset_id est obligatoire");
        }
        if (isBlank(description)) {
            throw MaintenanceException.invalidArgument("description est obligatoire");
        }
        MaintenanceRequestEntity request = new MaintenanceRequestEntity(
                UUID.randomUUID().toString(), assetId.trim(), description.trim(), Instant.now().truncatedTo(ChronoUnit.MILLIS),
                MaintenanceRequestStatus.OPEN);
        log.info("Demande de maintenance {} créée pour l'équipement {}", request.getId(), request.getAssetId());
        return requestRepository.save(request);
    }

    @Transactional
    public InterventionEntity scheduleIntervention(String maintenanceRequestId, Instant scheduledAt) {
        if (isBlank(maintenanceRequestId)) {
            throw MaintenanceException.invalidArgument("maintenance_request_id est obligatoire");
        }
        if (scheduledAt == null) {
            throw MaintenanceException.invalidArgument("scheduled_at est obligatoire");
        }
        if (scheduledAt.isBefore(Instant.now().minus(CLOCK_SKEW))) {
            throw MaintenanceException.invalidArgument("scheduled_at ne peut pas être dans le passé");
        }
        MaintenanceRequestEntity request = requestRepository.findById(maintenanceRequestId)
                .orElseThrow(() -> MaintenanceException.notFound(
                        "Demande de maintenance introuvable : " + maintenanceRequestId));
        if (request.getStatus() != MaintenanceRequestStatus.OPEN) {
            throw MaintenanceException.failedPrecondition(
                    "La demande " + maintenanceRequestId + " n'est pas ouverte (statut : " + request.getStatus() + ")");
        }

        InterventionEntity intervention = new InterventionEntity(
                UUID.randomUUID().toString(), request.getId(), request.getAssetId(), scheduledAt.truncatedTo(ChronoUnit.MILLIS),
                InterventionStatus.SCHEDULED);
        request.setStatus(MaintenanceRequestStatus.SCHEDULED);
        log.info("Intervention {} planifiée le {} pour la demande {}", intervention.getId(), scheduledAt, request.getId());
        return interventionRepository.save(intervention);
    }

    @Transactional(readOnly = true)
    public InterventionEntity getIntervention(String interventionId) {
        if (isBlank(interventionId)) {
            throw MaintenanceException.invalidArgument("intervention_id est obligatoire");
        }
        return interventionRepository.findById(interventionId)
                .orElseThrow(() -> MaintenanceException.notFound("Intervention introuvable : " + interventionId));
    }

    @Transactional
    public InterventionEntity updateInterventionStatus(String interventionId, InterventionStatus target) {
        if (target == null) {
            throw MaintenanceException.invalidArgument("status est obligatoire");
        }
        InterventionEntity intervention = getIntervention(interventionId);
        InterventionStatus previous = intervention.getStatus();
        if (!previous.canTransitionTo(target)) {
            throw MaintenanceException.failedPrecondition(
                    "Transition interdite : " + previous + " -> " + target
                            + " (autorisées : " + previous.allowedTransitions() + ")");
        }
        intervention.setStatus(target);

        // Répercussion sur la demande de maintenance associée
        requestRepository.findById(intervention.getMaintenanceRequestId()).ifPresent(request -> {
            if (target == InterventionStatus.COMPLETED) {
                request.setStatus(MaintenanceRequestStatus.RESOLVED);
            } else if (target == InterventionStatus.CANCELLED) {
                // La demande redevient ouverte pour pouvoir être replanifiée
                request.setStatus(MaintenanceRequestStatus.OPEN);
            }
        });

        InterventionStatusChange change = new InterventionStatusChange(
                intervention.getId(), previous, target, Instant.now());
        publishAfterCommit(change);
        log.info("Intervention {} : {} -> {}", interventionId, previous, target);
        return intervention;
    }

    private void publishAfterCommit(InterventionStatusChange change) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.publish(change);
                }
            });
        } else {
            publisher.publish(change);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
