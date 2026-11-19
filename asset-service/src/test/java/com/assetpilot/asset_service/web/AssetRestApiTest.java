package com.assetpilot.asset_service.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de l'API REST (CRUD, validation, règles métier, OpenAPI).
 */
@SpringBootTest(properties = "assetpilot.seed-data=false")
@AutoConfigureMockMvc
@DirtiesContext
class AssetRestApiTest {

    @Autowired
    private MockMvc mvc;

    private static final String LAPTOP = """
            {"assetTag":"%s","name":"Laptop test","type":"LAPTOP","brand":"Dell","model":"X",
             "serialNumber":"%s","purchaseDate":"2025-01-10"}
            """;

    private long createEquipment(String tag, String serial) throws Exception {
        String body = mvc.perform(post("/api/v1/equipments").contentType(MediaType.APPLICATION_JSON)
                        .content(LAPTOP.formatted(tag, serial)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private long createEmployee(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Test","lastName":"User","email":"%s","department":"IT"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    @DisplayName("CRUD équipement + unicité assetTag (409) + 404")
    void equipmentCrud() throws Exception {
        long id = createEquipment("T-100", "SN-T-100");

        mvc.perform(get("/api/v1/equipments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetTag").value("T-100"));

        mvc.perform(post("/api/v1/equipments").contentType(MediaType.APPLICATION_JSON)
                        .content(LAPTOP.formatted("T-100", "SN-OTHER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Règle métier non respectée"));

        mvc.perform(put("/api/v1/equipments/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(LAPTOP.formatted("T-100", "SN-T-100").replace("Laptop test", "Laptop renommé")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop renommé"));

        mvc.perform(delete("/api/v1/equipments/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/equipments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Validation : 400 avec le détail des champs invalides")
    void validationErrors() throws Exception {
        mvc.perform(post("/api/v1/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"X\",\"email\":\"pas-un-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        mvc.perform(post("/api/v1/equipments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetTag\":\"A\",\"name\":\"n\",\"type\":\"TOASTER\",\"serialNumber\":\"s\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Affectation : équipement ASSIGNED puis AVAILABLE après restitution")
    void assignmentLifecycle() throws Exception {
        long equipmentId = createEquipment("T-200", "SN-T-200");
        long employeeId = createEmployee("assign@test.tn");

        String body = mvc.perform(post("/api/v1/assignments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipmentId\":%d,\"employeeId\":%d}".formatted(equipmentId, employeeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.assetTag").value("T-200"))
                .andReturn().getResponse().getContentAsString();
        long assignmentId = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(get("/api/v1/equipments/{id}", equipmentId)).andExpect(jsonPath("$.status").value("ASSIGNED"));

        // Un équipement déjà affecté ne peut pas l'être une seconde fois
        mvc.perform(post("/api/v1/assignments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipmentId\":%d,\"employeeId\":%d}".formatted(equipmentId, employeeId)))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/assignments").param("employeeId", String.valueOf(employeeId)))
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(patch("/api/v1/assignments/{id}/return", assignmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
        mvc.perform(get("/api/v1/equipments/{id}", equipmentId)).andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Documentation OpenAPI exposée sur /v3/api-docs")
    void openApiDocs() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("AssetPilot — Asset Service API"))
                .andExpect(jsonPath("$.paths['/api/v1/equipments']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipments/{id}/maintenance-requests']").exists());
    }
}
