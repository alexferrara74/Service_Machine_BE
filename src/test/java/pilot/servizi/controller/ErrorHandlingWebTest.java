package pilot.servizi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import pilot.servizi.repository.UtenteRepository;
import pilot.servizi.security.JwtService;
import pilot.servizi.security.UtenteJwtConverter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.service.AppuntamentoAssistenzaService;
import pilot.servizi.service.AppuntamentoExportService;
import pilot.servizi.service.ClienteStatisticheService;
import pilot.servizi.service.ClientiService;

import java.sql.SQLException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica che GlobalExceptionHandler traduca gli errori del client in 4xx e non in 500. */
@Import({JwtService.class, UtenteJwtConverter.class}) // SecurityConfig viene caricata comunque: ha bisogno di questi bean
@TestPropertySource(properties = "security.jwt.secret=test-secret-test-secret-test-secret-0123456789")
@AutoConfigureMockMvc(addFilters = false) // qui si prova la traduzione degli errori; le regole di accesso sono in SecurityWebTest
@WebMvcTest({ClienteController.class, ClienteStatisticheController.class, AppuntamentoController.class})
class ErrorHandlingWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UtenteRepository utenteRepository;

    @MockitoBean
    private ClientiService clientiService;

    @MockitoBean
    private ClienteStatisticheService statisticheService;

    @MockitoBean
    private AppuntamentoAssistenzaService appuntamentoService;

    @MockitoBean
    private AppuntamentoExportService appuntamentoExportService;

    @Test
    void nonNumericPathVariableIsABadRequest() throws Exception {
        mvc.perform(get("/api/clienti/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Valore non valido per il parametro 'id'"));
    }

    @Test
    void malformedDateParameterIsABadRequest() throws Exception {
        mvc.perform(get("/api/statistiche/clienti/1").param("dal", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void unreadableJsonBodyIsABadRequest() throws Exception {
        mvc.perform(post("/api/clienti").contentType(MediaType.APPLICATION_JSON).content("{non json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void decimalOnAnIntegerFieldIsRejectedAndNamesTheField() throws Exception {
        mvc.perform(post("/api/clienti").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codice\":\"C1\",\"ragioneSociale\":\"Rossi\",\"tipoListino\":1.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valore non valido per il campo 'tipoListino'"));
    }

    @Test
    void unknownRouteIsNotFoundInsteadOfServerError() throws Exception {
        mvc.perform(get("/api/non-esiste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Risorsa non trovata"));
    }

    @Test
    void unsupportedMethodIsMethodNotAllowed() throws Exception {
        mvc.perform(post("/api/clienti/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Metodo HTTP non supportato per questo percorso"));
    }

    @Test
    void missingResourceIsNotFound() throws Exception {
        when(clientiService.getById(9L)).thenThrow(new ResourceNotFoundException("Cliente non trovato con ID 9"));

        mvc.perform(get("/api/clienti/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente non trovato con ID 9"));
    }

    @Test
    void businessConflictKeepsItsUserMessage() throws Exception {
        doThrow(new DataIntegrityViolationException("Il cliente non può essere eliminato"))
                .when(clientiService).delete(3L);

        mvc.perform(delete("/api/clienti/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Il cliente non può essere eliminato"));
    }

    @Test
    void databaseConflictDoesNotLeakSql() throws Exception {
        doThrow(new DataIntegrityViolationException(
                "could not execute statement [Cannot delete or update a parent row: constraint fk_x] [delete from local_clienti]",
                new SQLException("Cannot delete or update a parent row")))
                .when(clientiService).delete(3L);

        mvc.perform(delete("/api/clienti/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("L'operazione viola un vincolo di integrità dei dati"));
    }

    @Test
    void unexpectedFailureIsAGenericServerError() throws Exception {
        when(clientiService.getById(1L)).thenThrow(new RuntimeException("dettaglio interno"));

        mvc.perform(get("/api/clienti/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Si è verificato un errore interno nel server"));
    }

    @Test
    void calendarRangeWithoutDatesIsABadRequest() throws Exception {
        mvc.perform(get("/api/appuntamenti"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownAppointmentStatusIsABadRequest() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/appuntamenti/1/stato")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stato\":\"BOH\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void appointmentWithoutTitleIsRejectedByValidation() throws Exception {
        mvc.perform(post("/api/appuntamenti").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stato\":\"DA_FARE\",\"inizio\":\"2026-11-20T09:00:00\",\"fine\":\"2026-11-20T10:00:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void exportWithAnUnknownFormatIsABadRequest() throws Exception {
        mvc.perform(get("/api/appuntamenti/export").param("da", "2026-11-16T00:00:00").param("a", "2026-11-23T00:00:00").param("formato", "doc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
