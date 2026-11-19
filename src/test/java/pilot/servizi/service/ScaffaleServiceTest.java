package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pilot.servizi.dto.request.ScaffaleRequest;
import pilot.servizi.dto.response.ScaffaleLayoutResponse;
import pilot.servizi.entity.MagazzinoDigitale;
import pilot.servizi.entity.MagazzinoScaffale;
import pilot.servizi.repository.MagazzinoDigitaleRepository;
import pilot.servizi.repository.MagazzinoPosizioniArticoliRepository;
import pilot.servizi.repository.MagazzinoScaffaleRepository;
import pilot.servizi.repository.ProdottiCatalogoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScaffaleServiceTest {

    @Mock private MagazzinoScaffaleRepository repo;
    @Mock private MagazzinoDigitaleRepository ubicazioniRepo;
    @Mock private MagazzinoPosizioniArticoliRepository posizioniRepo;
    @Mock private ProdottiCatalogoRepository catalogoRepo;
    @InjectMocks private ScaffaleService service;

    private ScaffaleRequest request(String codice, int ripiani, int posizioni, int rotazione) {
        return new ScaffaleRequest(codice, "Zona A", 200, 60, 250, 100, 50, rotazione, ripiani, posizioni);
    }

    @Test
    void generatesOneLabelPerCell() {
        when(repo.existsByCodice("A01")).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> {
            MagazzinoScaffale s = i.getArgument(0);
            s.setScaffaleId(7L);
            return s;
        });
        when(ubicazioniRepo.findByCodiceUbicazione(any())).thenReturn(Optional.empty());

        ScaffaleLayoutResponse out = service.create(request("a01", 2, 3, 0));

        assertEquals("A01", out.codice());
        assertEquals(6, out.celle().size());
        assertEquals("A01-R01-P01", out.celle().get(0).etichetta());
        assertEquals("A01-R02-P03", out.celle().get(5).etichetta());
        ArgumentCaptor<List<MagazzinoDigitale>> saved = ArgumentCaptor.forClass(List.class);
        verify(ubicazioniRepo).saveAll(saved.capture());
        assertEquals(7L, saved.getValue().get(0).getScaffaleId());
    }

    @Test
    void generatesTheCodeWhenMissing() {
        when(repo.count()).thenReturn(0L);
        when(repo.existsByCodice("S01")).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ubicazioniRepo.findByCodiceUbicazione(any())).thenReturn(Optional.empty());

        assertEquals("S01", service.create(request(null, 1, 1, 90)).codice());
    }

    @Test
    void rejectsADuplicateCode() {
        when(repo.existsByCodice("A01")).thenReturn(true);
        assertThrows(DataIntegrityViolationException.class, () -> service.create(request("A01", 1, 1, 0)));
        verify(repo, never()).save(any());
    }

    @Test
    void rejectsAnInvalidRotation() {
        assertThrows(IllegalArgumentException.class, () -> service.create(request("A01", 1, 1, 45)));
    }

    @Test
    void refusesToShrinkOverOccupiedCells() {
        MagazzinoScaffale s = MagazzinoScaffale.builder().scaffaleId(1L).codice("A01").zona("Zona A")
                .larghezzaCm(200).profonditaCm(60).altezzaCm(250).posXCm(0).posYCm(0).rotazioneGradi(0)
                .numeroRipiani(2).posizioniPerRipiano(1).build();
        MagazzinoDigitale bassa = cella(10L, 1, "A01-R01-P01");
        MagazzinoDigitale alta = cella(11L, 2, "A01-R02-P01");
        when(repo.findById(1L)).thenReturn(Optional.of(s));
        when(repo.existsByCodiceAndScaffaleIdNot("A01", 1L)).thenReturn(false);
        when(ubicazioniRepo.findByScaffaleIdOrderByRipianoNumAscPosizioneNumAsc(1L)).thenReturn(List.of(bassa, alta));
        when(posizioniRepo.existsByUbicazioneId(11L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.update(1L, request("A01", 1, 1, 0)));
        verify(ubicazioniRepo, never()).deleteAll(any());
    }

    @Test
    void refusesToDeleteAShelfWithStock() {
        MagazzinoScaffale s = MagazzinoScaffale.builder().scaffaleId(1L).codice("A01").build();
        when(repo.findById(1L)).thenReturn(Optional.of(s));
        when(ubicazioniRepo.findByScaffaleIdOrderByRipianoNumAscPosizioneNumAsc(1L)).thenReturn(List.of(cella(10L, 1, "A01-R01-P01")));
        when(posizioniRepo.existsByUbicazioneId(10L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.delete(1L));
        verify(repo, never()).delete(any());
    }

    private MagazzinoDigitale cella(Long id, int ripiano, String codice) {
        MagazzinoDigitale c = new MagazzinoDigitale();
        c.setUbicazioneId(id);
        c.setRipianoNum(ripiano);
        c.setPosizioneNum(1);
        c.setCodiceUbicazione(codice);
        return c;
    }
}
