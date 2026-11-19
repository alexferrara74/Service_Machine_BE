package pilot.servizi.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pilot.servizi.controller.ClienteController;
import pilot.servizi.repository.UtenteRepository;
import pilot.servizi.security.JwtService;
import pilot.servizi.security.SecurityConfig;
import pilot.servizi.security.UtenteJwtConverter;
import pilot.servizi.service.ClientiService;
import pilot.servizi.service.RegistroService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Il BE serve il frontend da WEB_DIR senza aprire nulla delle API. */
@WebMvcTest(ClienteController.class)
@Import({WebConfig.class, SecurityConfig.class, JwtService.class, UtenteJwtConverter.class})
@TestPropertySource(properties = "security.jwt.secret=test-secret-test-secret-test-secret-0123456789")
class WebConfigTest {

    private static final Path WEB = creaFrontendFinto();

    @DynamicPropertySource
    static void webDir(DynamicPropertyRegistry registry) {
        registry.add("app.web-dir", WEB::toString);
    }

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private UtenteRepository utenti;
    @MockitoBean
    private RegistroService registro;
    @MockitoBean
    private ClientiService clientiService;

    private static Path creaFrontendFinto() {
        try {
            Path dir = Files.createTempDirectory("web");
            Files.writeString(dir.resolve("index.csr.html"), "<app-root>indice</app-root>");
            Files.writeString(dir.resolve("main-ABC.js"), "console.log('ok')");
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void serveIFileDelFrontendSenzaLogin() throws Exception {
        mvc.perform(get("/main-ABC.js")).andExpect(status().isOk()).andExpect(content().string("console.log('ok')"));
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.csr.html")); // MockMvc non esegue il forward
    }

    @Test
    void leRotteDiAngularRitornanoLIndice() throws Exception {
        mvc.perform(get("/appuntamenti/2/5")).andExpect(status().isOk()).andExpect(content().string("<app-root>indice</app-root>"));
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string("<app-root>indice</app-root>"));
    }

    @Test
    void leApiRestanoProtetteENonDiventanoLIndice() throws Exception {
        mvc.perform(get("/api/clienti")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/clienti").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/qualsiasi")).andExpect(status().isUnauthorized()); // solo le GET sono pubbliche
    }
}
