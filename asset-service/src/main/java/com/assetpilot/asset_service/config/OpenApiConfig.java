package com.assetpilot.asset_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI assetPilotOpenApi(@Value("${assetpilot.gateway-url:http://localhost:8083}") String gatewayUrl,
                                     @Value("${server.port:8081}") int port) {
        return new OpenAPI()
                .info(new Info()
                        .title("AssetPilot — Asset Service API")
                        .version("v1")
                        .description("""
                                Gestion du parc informatique : équipements, employés et affectations.
                                Les endpoints de garantie et de maintenance appellent le Warranty Service (SOAP)
                                et le Maintenance Service (gRPC).""")
                        .contact(new Contact().name("Équipe AssetPilot — 3 GLID G1")))
                .servers(List.of(
                        new Server().url(gatewayUrl).description("Via l'API Gateway"),
                        new Server().url("http://localhost:" + port).description("Asset Service en direct")));
    }
}
