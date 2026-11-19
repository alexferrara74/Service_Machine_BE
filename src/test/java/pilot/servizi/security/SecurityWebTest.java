package pilot.servizi.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pilot.servizi.controller.AppuntamentoController;
import pilot.servizi.controller.AuthController;
import pilot.servizi.controller.ClienteController;
import pilot.servizi.controller.ErpSyncController;
import pilot.servizi.controller.NoleggioAppuntamentoController;
import pilot.servizi.controller.RegistroController;
import pilot.servizi.service.RegistroService;
import pilot.servizi.controller.UtenteController;
import pilot.servizi.dto.response.LoginResponse;
import pilot.servizi.entity.Utente;
import pilot.servizi.entity.Utente.Ruolo;
import pilot.servizi.repository.UtenteRepository;
import pilot.servizi.service.AppuntamentoAssistenzaService;
import pilot.servizi.service.AppuntamentoExportService;
import pilot.servizi.service.AuthService;
import pilot.servizi.service.NoleggioAppuntamentoService;
import pilot.servizi.service.ClientiService;
import pilot.servizi.service.ErpSyncProxyService;
import pilot.servizi.service.UtenteService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Le regole di accesso: ADMIN può tutto, VIEWER solo leggere, chi non ha il token non entra. */
@WebMvcTest({ClienteController.class, UtenteController.class, AuthController.class, ErpSyncController.class, RegistroController.class,
        AppuntamentoController.class, NoleggioAppuntamentoController.class})
@Import({SecurityConfig.class, JwtService.class, UtenteJwtConverter.class})
@TestPropertySource(properties = "security.jwt.secret=test-secret-test-secret-test-secret-0123456789")
class SecurityWebTest {

    private static final String CLIENTE = "{\"codice\":\"C1\",\"ragioneSociale\":\"Rossi Srl\"}";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JwtService jwt;

    @MockitoBean
    private UtenteRepository utenti;
    @MockitoBean
    private ClientiService clientiService;
    @MockitoBean
    private UtenteService utenteService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private ErpSyncProxyService erpSync;
    @MockitoBean
    private RegistroService registro;
    @MockitoBean
    private AppuntamentoAssistenzaService appuntamenti;
    @MockitoBean
    private AppuntamentoExportService appuntamentiExport;
    @MockitoBean
    private NoleggioAppuntamentoService noleggioAppuntamenti;

    private Utente utente(String username, Ruolo ruolo, boolean attivo) {
        Utente u = Utente.builder().id(1L).username(username).passwordHash("x").ruolo(ruolo).attivo(attivo)
                .passwordModificataIl(LocalDateTime.now().minusDays(1)).creatoIl(LocalDateTime.now().minusDays(2)).build();
        when(utenti.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(u));
        return u;
    }

    private String bearer(Utente u) {
        return "Bearer " + jwt.emetti(u).token();
    }

    @Test
    void withoutATokenEveryApiCallIsUnauthorized() throws Exception {
        mvc.perform(get("/api/clienti"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
        mvc.perform(post("/api/clienti").contentType(MediaType.APPLICATION_JSON).content(CLIENTE))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(clientiService);
    }

    @Test
    void aGarbageTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/clienti").header("Authorization", "Bearer non.un.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginIsPublic() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("t", null, "mario", Ruolo.VIEWER));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mario\",\"password\":\"password-lunga-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruolo").value("VIEWER"));
    }

    @Test
    void aViewerCanRead() throws Exception {
        Utente viewer = utente("viewer", Ruolo.VIEWER, true);
        when(clientiService.getAll()).thenReturn(List.of());

        mvc.perform(get("/api/clienti").header("Authorization", bearer(viewer)))
                .andExpect(status().isOk());
    }

    @Test
    void aViewerCannotModifyAnything() throws Exception {
        String auth = bearer(utente("viewer", Ruolo.VIEWER, true));

        mvc.perform(post("/api/clienti").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(CLIENTE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        mvc.perform(put("/api/clienti/1").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(CLIENTE))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/clienti/1").header("Authorization", auth))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/sync/erp").header("Authorization", auth))
                .andExpect(status().isForbidden());
        verify(clientiService, never()).create(any());
        verify(clientiService, never()).delete(any());
        verifyNoInteractions(erpSync);
    }

    @Test
    void aTecnicoReadsEverythingButCannotModifyOutsideAppointments() throws Exception {
        String auth = bearer(utente("tecnico", Ruolo.TECNICO, true));
        when(clientiService.getAll()).thenReturn(List.of());

        mvc.perform(get("/api/clienti").header("Authorization", auth)).andExpect(status().isOk());
        mvc.perform(post("/api/clienti").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(CLIENTE))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/clienti/1").header("Authorization", auth)).andExpect(status().isForbidden());
        mvc.perform(get("/api/utenti").header("Authorization", auth)).andExpect(status().isForbidden());
        mvc.perform(post("/api/sync/erp").header("Authorization", auth)).andExpect(status().isForbidden());
        verify(clientiService, never()).create(any());
    }

    @Test
    void aTecnicoConfirmsClosesWritesAndAttachesPhotosOnAppointments() throws Exception {
        String auth = bearer(utente("tecnico", Ruolo.TECNICO, true));
        String chiudi = "{\"stato\":\"FATTO\"}";
        when(appuntamenti.updateStato(eq(5L), any())).thenReturn(null);

        mvc.perform(patch("/api/appuntamenti/5/stato").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(chiudi))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/appuntamenti/5/stato").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content("{\"stato\":\"DA_FARE\"}"))
                .andExpect(status().isOk());
        mvc.perform(multipart("/api/appuntamenti/5/foto").file(new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1})).header("Authorization", auth))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/appuntamenti/5/foto/9").header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(multipart("/api/storia/noleggi/3/appuntamenti/5/foto").file(new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1})).header("Authorization", auth))
                .andExpect(status().isCreated());
    }

    @Test
    void aTecnicoCannotCreateDeleteOrCancelAppointments() throws Exception {
        String auth = bearer(utente("tecnico", Ruolo.TECNICO, true));
        String nuovo = "{\"titolo\":\"T\",\"stato\":\"DA_FARE\",\"inizio\":\"2026-11-20T09:00:00\",\"fine\":\"2026-11-20T10:00:00\",\"clienteNome\":\"X\"}";
        String annullato = nuovo.replace("DA_FARE", "ANNULLATO");

        mvc.perform(post("/api/appuntamenti").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(nuovo))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/appuntamenti/5").header("Authorization", auth)).andExpect(status().isForbidden());
        mvc.perform(post("/api/storia/noleggi/3/appuntamenti/5/annulla").header("Authorization", auth)).andExpect(status().isForbidden());
        // annullare, anche con una modifica o col cambio di stato, resta all'ufficio
        mvc.perform(patch("/api/appuntamenti/5/stato").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content("{\"stato\":\"ANNULLATO\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.error").value("Forbidden"));
        mvc.perform(put("/api/appuntamenti/5").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(annullato))
                .andExpect(status().isForbidden());
        verify(appuntamenti, never()).create(any());
        verify(appuntamenti, never()).delete(any());
        verify(appuntamenti, never()).updateStato(any(), eq(pilot.servizi.entity.AppuntamentoAssistenza.Stato.ANNULLATO));
    }

    @Test
    void aViewerCannotTouchAppointments() throws Exception {
        String auth = bearer(utente("viewer", Ruolo.VIEWER, true));

        mvc.perform(patch("/api/appuntamenti/5/stato").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content("{\"stato\":\"FATTO\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/appuntamenti/5/foto").file(new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1})).header("Authorization", auth))
                .andExpect(status().isForbidden());
    }

    @Test
    void anAdminCanModify() throws Exception {
        String auth = bearer(utente("boss", Ruolo.ADMIN, true));

        mvc.perform(post("/api/clienti").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(CLIENTE))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/clienti/1").header("Authorization", auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void onlyAdminsCanManageUsers() throws Exception {
        when(utenteService.getAll()).thenReturn(List.of());

        mvc.perform(get("/api/utenti").header("Authorization", bearer(utente("viewer", Ruolo.VIEWER, true))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/utenti").header("Authorization", bearer(utente("boss", Ruolo.ADMIN, true))))
                .andExpect(status().isOk());
    }

    @Test
    void aViewerCanChangeTheirOwnPassword() throws Exception {
        String auth = bearer(utente("viewer", Ruolo.VIEWER, true));

        mvc.perform(post("/api/auth/password").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordAttuale\":\"vecchia-password\",\"nuovaPassword\":\"nuova-password-1\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void aDisabledUserLosesAccessEvenWithAValidToken() throws Exception {
        Utente u = utente("viewer", Ruolo.VIEWER, true);
        String auth = bearer(u);
        u.setAttivo(false);

        mvc.perform(get("/api/clienti").header("Authorization", auth)).andExpect(status().isUnauthorized());
    }

    @Test
    void aDeletedUserLosesAccessEvenWithAValidToken() throws Exception {
        String auth = bearer(utente("viewer", Ruolo.VIEWER, true));
        when(utenti.findByUsernameIgnoreCase("viewer")).thenReturn(Optional.empty());

        mvc.perform(get("/api/clienti").header("Authorization", auth)).andExpect(status().isUnauthorized());
    }

    @Test
    void theRoleComesFromTheDatabaseNotFromTheToken() throws Exception {
        Utente u = utente("boss", Ruolo.ADMIN, true);
        String auth = bearer(u);
        u.setRuolo(Ruolo.VIEWER); // declassato dopo l'emissione del token

        mvc.perform(post("/api/clienti").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(CLIENTE))
                .andExpect(status().isForbidden());
    }

    @Test
    void aTokenIssuedBeforeAPasswordChangeIsRejected() throws Exception {
        Utente u = utente("viewer", Ruolo.VIEWER, true);
        String auth = bearer(u);
        u.setPasswordModificataIl(LocalDateTime.now().plusMinutes(1));

        mvc.perform(get("/api/clienti").header("Authorization", auth)).andExpect(status().isUnauthorized());
    }

    @Test
    void onlyAdminsCanReadTheRegistry() throws Exception {
        mvc.perform(get("/api/registro").header("Authorization", bearer(utente("viewer", Ruolo.VIEWER, true))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/registro").header("Authorization", bearer(utente("boss", Ruolo.ADMIN, true))))
                .andExpect(status().isOk());
    }

    @Test
    void writesAreRecordedWithWhoDidWhatAndTheOutcome() throws Exception {
        String boss = bearer(utente("boss", Ruolo.ADMIN, true));
        mvc.perform(delete("/api/clienti/7").header("Authorization", boss)).andExpect(status().isNoContent());

        verify(registro).registraRichiesta(eq("boss"), eq("ADMIN"), eq("DELETE"), eq("/api/clienti/7"), eq(204), any());
    }

    @Test
    void aForbiddenWriteByAViewerIsRecordedAsAttempt() throws Exception {
        String auth = bearer(utente("viewer", Ruolo.VIEWER, true));
        mvc.perform(post("/api/clienti").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(CLIENTE))
                .andExpect(status().isForbidden());

        verify(registro).registraRichiesta(eq("viewer"), eq("VIEWER"), eq("POST"), eq("/api/clienti"), eq(403), any());
    }

    @Test
    void plainReadsAnonymousCallsAndLoginAreNotRecordedByTheFilter() throws Exception {
        when(clientiService.getAll()).thenReturn(List.of());
        mvc.perform(get("/api/clienti").header("Authorization", bearer(utente("viewer", Ruolo.VIEWER, true)))).andExpect(status().isOk());
        mvc.perform(post("/api/clienti").contentType(MediaType.APPLICATION_JSON).content(CLIENTE)).andExpect(status().isUnauthorized());
        when(authService.login(any())).thenReturn(new LoginResponse("t", null, "mario", Ruolo.VIEWER));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"mario\",\"password\":\"password-lunga-1\"}")).andExpect(status().isOk());

        verify(registro, never()).registraRichiesta(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any());
    }
}
