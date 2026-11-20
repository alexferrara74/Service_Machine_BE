package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pilot.servizi.dto.request.StoriaNoleggioRequest;
import pilot.servizi.entity.Attrezzature;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.ProdottiCatalogoRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoriaNoleggiServiceTest {

    @Mock
    private StoriaNoleggiRepository repo;
    @Mock
    private ClientiRepository clientiRepo;
    @Mock
    private ProdottiCatalogoRepository prodottiRepo;
    @Mock
    private AttrezzatureRepository attrezzatureRepo;
    @Mock
    private pilot.servizi.repository.AppuntamentoAssistenzaRepository appuntamentiRepo;
    @Mock
    private pilot.servizi.repository.NoleggioAppuntamentoRepository appuntamentiNoleggioRepo;
    @Mock
    private pilot.servizi.repository.NoleggioManutenzioneRepository manutenzioniRepo;

    @InjectMocks
    private StoriaNoleggiService service;

    private StoriaNoleggioRequest richiesta(long articoloId, String matricola) {
        return new StoriaNoleggioRequest(null, LocalDate.of(2026, 11, 1), 9L, articoloId, matricola, 1.0);
    }

    private void riferimentiEsistono(Attrezzature a) {
        when(clientiRepo.existsById(9L)).thenReturn(true);
        when(prodottiRepo.existsById(a.getArticoloId() != null ? a.getArticoloId() : 7L)).thenReturn(true);
        when(attrezzatureRepo.findByMatricola(a.getMatricola())).thenReturn(Optional.of(a));
    }

    private Attrezzature attrezzatura(String matricola, Long articoloId) {
        Attrezzature a = new Attrezzature();
        a.setMatricola(matricola);
        a.setArticoloId(articoloId);
        return a;
    }

    @Test
    void acceptsARentalWhenTheSerialNumberBelongsToThatArticle() {
        riferimentiEsistono(attrezzatura("M-1", 7L));
        when(repo.save(any())).thenAnswer(i -> { StoriaNoleggi e = i.getArgument(0); e.setDocId(1); return e; });

        assertEquals(7L, service.create(richiesta(7L, "M-1")).articoloId());
    }

    @Test
    void acceptsARentalForASerialNumberWithoutALink() {
        when(clientiRepo.existsById(9L)).thenReturn(true);
        when(prodottiRepo.existsById(7L)).thenReturn(true);
        when(attrezzatureRepo.findByMatricola("M-2")).thenReturn(Optional.of(attrezzatura("M-2", null)));
        when(repo.save(any())).thenAnswer(i -> { StoriaNoleggi e = i.getArgument(0); e.setDocId(2); return e; });

        assertEquals("M-2", service.create(richiesta(7L, "M-2")).matricola());
    }

    @Test
    void rejectsARentalWhenTheSerialNumberBelongsToAnotherArticle() {
        when(clientiRepo.existsById(9L)).thenReturn(true);
        when(prodottiRepo.existsById(8L)).thenReturn(true);
        when(attrezzatureRepo.findByMatricola("M-1")).thenReturn(Optional.of(attrezzatura("M-1", 7L)));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.create(richiesta(8L, "M-1")));

        assertEquals(true, e.getMessage().contains("ID 7"));
        verify(repo, never()).save(any());
    }

    @Test
    void rejectsAnUnknownSerialNumber() {
        when(clientiRepo.existsById(9L)).thenReturn(true);
        when(prodottiRepo.existsById(7L)).thenReturn(true);
        when(attrezzatureRepo.findByMatricola("M-X")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(richiesta(7L, "M-X")));
    }

    // ---- eliminazione: il lavoro già svolto non sparisce in silenzio ---------------------------

    @Test
    void refusesToDeleteARentalWithAClosedCalendarAppointment() {
        when(repo.existsById(5)).thenReturn(true);
        when(appuntamentiRepo.existsByDocIdAndStato(5, pilot.servizi.entity.AppuntamentoAssistenza.Stato.FATTO)).thenReturn(true);

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> service.delete(5));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void refusesToDeleteARentalWithRecordedMaintenance() {
        when(repo.existsById(5)).thenReturn(true);
        when(manutenzioniRepo.existsByDocId(5)).thenReturn(true);

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> service.delete(5));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void deletesARentalWithNothingClosed() {
        when(repo.existsById(5)).thenReturn(true);

        service.delete(5);

        verify(repo).deleteById(5);
    }
}
