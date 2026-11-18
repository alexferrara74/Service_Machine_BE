package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pilot.servizi.repository.MagazzinoDigitaleRepository;
import pilot.servizi.repository.MagazzinoPosizioniArticoliRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MagazzinoDigitaleServiceTest {

    @Mock
    private MagazzinoDigitaleRepository repo;
    @Mock
    private MagazzinoPosizioniArticoliRepository posizioniRepo;

    @InjectMocks
    private MagazzinoDigitaleService service;

    @Test
    void deletesAnEmptyLocation() {
        when(repo.existsById(3L)).thenReturn(true);

        service.delete(3L);

        verify(repo).deleteById(3L);
    }

    @Test
    void refusesToDeleteALocationHoldingStock() {
        when(repo.existsById(3L)).thenReturn(true);
        when(posizioniRepo.existsByUbicazioneId(3L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.delete(3L));
        verify(repo, never()).deleteById(3L);
    }
}
