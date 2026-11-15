package com.assetpilot.maintenance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "grpc.server.port=0")
class MaintenanceApplicationTests {

    @Test
    void contextLoads() {
    }
}
