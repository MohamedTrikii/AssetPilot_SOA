package com.assetpilot.asset_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Deadlines appliqués aux appels gRPC vers le Maintenance Service.
 */
@ConfigurationProperties(prefix = "assetpilot.maintenance")
public record MaintenanceClientProperties(
        @DefaultValue("3s") Duration deadline,
        @DefaultValue("10m") Duration streamDeadline
) {
}
