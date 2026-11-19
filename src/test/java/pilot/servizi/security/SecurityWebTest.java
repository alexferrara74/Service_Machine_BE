package pilot.servizi.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pilot.servizi.controller.AuthController;
import pilot.servizi.controller.ClienteController;
import pilot.servizi.controller.ErpSyncController;
import pilot.servizi.controller.UtenteController;
import pilot.servizi.dto.response.LoginResponse;
import pilot.servizi.entity.Utente;
import pilot.servizi.entity.Utente.Ruolo;
import pilot.servizi.repository.UtenteRepository;
import pilot.servizi.service.AuthService;
import pilot.servizi.service.ClientiService;
import pilot.servizi.service.ErpSyncProxyService;
import pilot.servizi.service.UtenteService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Le regole di accesso: ADMIN può tutto, VIEWER solo leggere, chi non ha il token non entra. */
@WebMvcTest({ClienteController.class, UtenteController.class, AuthController.class, ErpSyncController.class})
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
}
