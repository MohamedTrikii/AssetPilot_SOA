package com.assetpilot.maintenance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Diffuse en mémoire les changements de statut aux abonnés (flux gRPC TrackInterventionStatus).
 */
@Component
public class InterventionStatusPublisher {

    private static final Logger log = LoggerFactory.getLogger(InterventionStatusPublisher.class);

    private final Map<String, Set<Consumer<InterventionStatusChange>>> subscribers = new ConcurrentHashMap<>();

    /**
     * Abonne un consommateur aux changements d'une intervention.
     *
     * @return action de désabonnement
     */
    public Runnable subscribe(String interventionId, Consumer<InterventionStatusChange> listener) {
        subscribers.computeIfAbsent(interventionId, id -> new CopyOnWriteArraySet<>()).add(listener);
        return () -> subscribers.computeIfPresent(interventionId, (id, set) -> {
            set.remove(listener);
            return set.isEmpty() ? null : set;
        });
    }

    public void publish(InterventionStatusChange change) {
        Set<Consumer<InterventionStatusChange>> listeners = subscribers.get(change.interventionId());
        if (listeners == null) {
            return;
        }
        for (Consumer<InterventionStatusChange> listener : listeners) {
            try {
                listener.accept(change);
            } catch (RuntimeException e) {
                log.warn("Abonné en erreur pour l'intervention {} : {}", change.interventionId(), e.getMessage());
            }
        }
    }

    int subscriberCount(String interventionId) {
        Set<Consumer<InterventionStatusChange>> listeners = subscribers.get(interventionId);
        return listeners == null ? 0 : listeners.size();
    }
}
