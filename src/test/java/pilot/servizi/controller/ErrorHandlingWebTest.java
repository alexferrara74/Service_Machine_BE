package pilot.servizi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pilot.servizi.log.ResourceNotFoundException;
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
@WebMvcTest({ClienteController.class, ClienteStatisticheController.class})
class ErrorHandlingWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClientiService clientiService;

    @MockitoBean
    private ClienteStatisticheService statisticheService;

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
}
