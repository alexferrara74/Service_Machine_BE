package pilot.servizi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pilot.servizi.dto.request.UtenteAggiornamentoRequest;
import pilot.servizi.dto.request.UtenteRequest;
import pilot.servizi.dto.response.UtenteResponse;
import pilot.servizi.entity.Utente;
import pilot.servizi.entity.Utente.Ruolo;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.UtenteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtenteServiceTest {

    @Mock
    private UtenteRepository repo;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private UtenteService service;

    @BeforeEach
    void setUp() {
        service = new UtenteService(repo, encoder);
    }

    private Utente utente(long id, String username, Ruolo ruolo, boolean attivo) {
        return Utente.builder().id(id).username(username).passwordHash(encoder.encode("password-iniziale"))
                .ruolo(ruolo).attivo(attivo).passwordModificataIl(LocalDateTime.now().minusDays(1)).creatoIl(LocalDateTime.now().minusDays(1)).build();
    }

    @Test
    void createsAUserStoringOnlyTheHashOfThePassword() {
        when(repo.existsByUsernameIgnoreCase("mario")).thenReturn(false);
        when(repo.save(any())).thenAnswer(c -> c.getArgument(0));

        UtenteResponse r = service.create(new UtenteRequest("  mario ", "una-password-lunga", Ruolo.VIEWER));

        ArgumentCaptor<Utente> saved = ArgumentCaptor.forClass(Utente.class);
        verify(repo).save(saved.capture());
        assertEquals("mario", saved.getValue().getUsername());
        assertNotEquals("una-password-lunga", saved.getValue().getPasswordHash());
        assertTrue(encoder.matches("una-password-lunga", saved.getValue().getPasswordHash()));
        assertTrue(saved.getValue().isAttivo());
        assertEquals(Ruolo.VIEWER, r.ruolo());
    }

    @Test
    void rejectsWeakPasswordsAndBadUsernames() {
        assertThrows(IllegalArgumentException.class, () -> service.create(new UtenteRequest("mario", "corta", Ruolo.VIEWER)));
        assertThrows(IllegalArgumentException.class, () -> service.create(new UtenteRequest("mario", "x".repeat(73), Ruolo.VIEWER)));
        assertThrows(IllegalArgumentException.class, () -> service.create(new UtenteRequest("mario.rossi", "MARIO.ROSSI", Ruolo.VIEWER)));
        assertThrows(IllegalArgumentException.class, () -> service.create(new UtenteRequest("a b", "una-password-lunga", Ruolo.VIEWER)));
        assertThrows(IllegalArgumentException.class, () -> service.create(new UtenteRequest("mario", "una-password-lunga", null)));
        verify(repo, never()).save(any());
    }

    @Test
    void rejectsADuplicateUsernameIgnoringCase() {
        when(repo.existsByUsernameIgnoreCase("Mario")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.create(new UtenteRequest("Mario", "una-password-lunga", Ruolo.ADMIN)));
        verify(repo, never()).save(any());
    }

    @Test
    void anAdminCannotDemoteOrDisableThemselves() {
        Utente io = utente(1, "boss", Ruolo.ADMIN, true);
        when(repo.findById(1L)).thenReturn(Optional.of(io));

        assertThrows(DataIntegrityViolationException.class, () -> service.update(1L, new UtenteAggiornamentoRequest(Ruolo.VIEWER, true, null), "BOSS"));
        assertThrows(DataIntegrityViolationException.class, () -> service.update(1L, new UtenteAggiornamentoRequest(Ruolo.ADMIN, false, null), "boss"));
        verify(repo, never()).save(any());
    }

    @Test
    void theLastActiveAdminCannotBeDemotedDisabledOrDeleted() {
        Utente ultimo = utente(2, "ultimo", Ruolo.ADMIN, true);
        when(repo.findById(2L)).thenReturn(Optional.of(ultimo));
        when(repo.countByRuoloAndAttivoTrue(Ruolo.ADMIN)).thenReturn(1L);

        assertThrows(DataIntegrityViolationException.class, () -> service.update(2L, new UtenteAggiornamentoRequest(Ruolo.VIEWER, true, null), "altro"));
        assertThrows(DataIntegrityViolationException.class, () -> service.update(2L, new UtenteAggiornamentoRequest(Ruolo.ADMIN, false, null), "altro"));
        assertThrows(DataIntegrityViolationException.class, () -> service.delete(2L, "altro"));
        verify(repo, never()).delete(any());
    }

    @Test
    void anAdminCanBeDemotedWhenAnotherOneExists() {
        Utente u = utente(2, "collega", Ruolo.ADMIN, true);
        when(repo.findById(2L)).thenReturn(Optional.of(u));
        when(repo.countByRuoloAndAttivoTrue(Ruolo.ADMIN)).thenReturn(2L);
        when(repo.save(any())).thenAnswer(c -> c.getArgument(0));

        UtenteResponse r = service.update(2L, new UtenteAggiornamentoRequest(Ruolo.VIEWER, true, null), "boss");

        assertEquals(Ruolo.VIEWER, r.ruolo());
    }

    @Test
    void aPasswordResetInvalidatesOlderTokens() {
        Utente u = utente(3, "mario", Ruolo.VIEWER, true);
        LocalDateTime prima = u.getPasswordModificataIl();
        when(repo.findById(3L)).thenReturn(Optional.of(u));
        when(repo.save(any())).thenAnswer(c -> c.getArgument(0));

        service.update(3L, new UtenteAggiornamentoRequest(Ruolo.VIEWER, true, "reset-password-1"), "boss");

        assertTrue(encoder.matches("reset-password-1", u.getPasswordHash()));
        assertTrue(u.getPasswordModificataIl().isAfter(prima));
    }

    @Test
    void aBlankNewPasswordLeavesThePasswordUntouched() {
        Utente u = utente(3, "mario", Ruolo.VIEWER, true);
        String hash = u.getPasswordHash();
        when(repo.findById(3L)).thenReturn(Optional.of(u));
        when(repo.save(any())).thenAnswer(c -> c.getArgument(0));

        service.update(3L, new UtenteAggiornamentoRequest(Ruolo.VIEWER, false, ""), "boss");

        assertEquals(hash, u.getPasswordHash());
    }

    @Test
    void aWeakResetPasswordIsRejected() {
        when(repo.findById(3L)).thenReturn(Optional.of(utente(3, "mario", Ruolo.VIEWER, true)));

        assertThrows(IllegalArgumentException.class, () -> service.update(3L, new UtenteAggiornamentoRequest(Ruolo.VIEWER, true, "corta"), "boss"));
    }

    @Test
    void anAdminCannotDeleteThemselvesButCanDeleteAViewer() {
        when(repo.findById(1L)).thenReturn(Optional.of(utente(1, "boss", Ruolo.ADMIN, true)));
        assertThrows(DataIntegrityViolationException.class, () -> service.delete(1L, "boss"));

        Utente viewer = utente(4, "viewer", Ruolo.VIEWER, true);
        when(repo.findById(4L)).thenReturn(Optional.of(viewer));
        service.delete(4L, "boss");
        verify(repo).delete(viewer);
    }

    @Test
    void reportsAMissingUser() {
        when(repo.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(9L, "boss"));
        assertThrows(ResourceNotFoundException.class, () -> service.update(9L, new UtenteAggiornamentoRequest(Ruolo.VIEWER, true, null), "boss"));
    }
}
