package com.assetpilot.asset_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuration du client SOAP vers le Warranty Service.
 */
@ConfigurationProperties(prefix = "assetpilot.warranty")
public record WarrantyClientProperties(
        @DefaultValue("http://localhost:8082/ws") String url,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("3s") Duration readTimeout
) {
}
