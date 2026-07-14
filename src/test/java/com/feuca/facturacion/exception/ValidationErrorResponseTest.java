package com.feuca.facturacion.exception;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidationErrorResponseTest {

    @Test
    void validationErrorsReturnBadRequestWithFieldMessages() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(patch("/probe")
                        .contentType("application/json")
                        .content("{\"cantidad\":0,\"ivaPorcentaje\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("La solicitud contiene errores de validacion."))
                .andExpect(jsonPath("$.path").value("/probe"))
                .andExpect(jsonPath("$.fieldErrors[0]").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data[0]").exists());
    }

    @Test
    void dataIntegrityViolationReturnsConflictWithFriendlyDuplicateMessage() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/probe/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya existe un cliente con ese email para esta empresa."))
                .andExpect(jsonPath("$.path").value("/probe/duplicate"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.data").value("Ya existe un cliente con ese email para esta empresa."));
    }

    @Test
    void unexpectedErrorsReturnGenericInternalServerErrorWithoutTechnicalDetails() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/probe/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocurrio un error inesperado."))
                .andExpect(jsonPath("$.data").value("Ocurrio un error inesperado."))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SELECT"))))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @RestController
    private static class ProbeController {
        @PatchMapping("/probe")
        ResponseEntity<Void> update(@RequestBody @Valid FacturaLineaUpdateRequest request) {
            return ResponseEntity.ok().build();
        }

        @PostMapping("/probe/duplicate")
        ResponseEntity<Void> duplicate() {
            throw new DataIntegrityViolationException(
                    "could not execute statement",
                    new RuntimeException("ERROR: duplicate key value violates unique constraint \"ux_clientes_empresa_email\"")
            );
        }

        @org.springframework.web.bind.annotation.GetMapping("/probe/unexpected")
        ResponseEntity<Void> unexpected() {
            throw new IllegalStateException("SELECT * FROM secret_table WHERE token = 'raw-jwt'");
        }
    }
}
