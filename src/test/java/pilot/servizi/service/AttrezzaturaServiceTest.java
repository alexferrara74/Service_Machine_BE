package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pilot.servizi.entity.Attrezzature;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;
import pilot.servizi.repository.InterventiRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttrezzaturaServiceTest {

    @Mock
    private AttrezzatureRepository repo;
    @Mock
    private StoriaNoleggiRepository noleggiRepo;
    @Mock
    private InterventiRepository interventiRepo;

    @InjectMocks
    private AttrezzaturaService service;

    private Attrezzature attrezzatura(String matricola) {
        Attrezzature e = new Attrezzature();
        e.setId(5L);
        e.setMatricola(matricola);
        return e;
    }

    @Test
    void deletesAnUnusedAttrezzatura() {
        when(repo.findById(5L)).thenReturn(Optional.of(attrezzatura("M-1")));

        service.delete(5L);

        verify(repo).deleteById(5L);
    }

    @Test
    void refusesToDeleteAnAttrezzaturaUsedByARental() {
        when(repo.findById(5L)).thenReturn(Optional.of(attrezzatura("M-1")));
        when(noleggiRepo.existsByMatricola("M-1")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.delete(5L));
        verify(repo, never()).deleteById(5L);
    }

    @Test
    void refusesToDeleteAnAttrezzaturaUsedByAnIntervention() {
        when(repo.findById(5L)).thenReturn(Optional.of(attrezzatura("M-1")));
        when(interventiRepo.existsByMatricolaAttrezzatura("M-1")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.delete(5L));
        verify(repo, never()).deleteById(5L);
    }

    @Test
    void deletesACatalogEntryWithoutSerialNumber() {
        when(repo.findById(5L)).thenReturn(Optional.of(attrezzatura(null)));

        service.delete(5L);

        verify(repo).deleteById(5L);
    }

    @Test
    void reportsAMissingAttrezzaturaOnDelete() {
        when(repo.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(5L));
    }
}
