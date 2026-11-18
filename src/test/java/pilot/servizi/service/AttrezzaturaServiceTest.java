package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pilot.servizi.dto.request.AttrezzaturaRequest;
import pilot.servizi.entity.Attrezzature;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;
import pilot.servizi.repository.InterventiRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void refusesToRenameAMatricolaUsedByARental() {
        when(repo.findById(5L)).thenReturn(Optional.of(attrezzatura("M-1")));
        when(noleggiRepo.existsByMatricola("M-1")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class,
                () -> service.update(5L, new AttrezzaturaRequest("M-2", null, null, null, null)));
        verify(repo, never()).save(any());
    }

    @Test
    void renamesAnUnusedMatricola() {
        when(repo.findById(5L)).thenReturn(Optional.of(attrezzatura("M-1")));
        when(repo.save(any())).thenAnswer(call -> call.getArgument(0));

        service.update(5L, new AttrezzaturaRequest("M-2", null, null, null, null));

        verify(repo).save(any());
    }

    @Test
    void acceptsAMatricolaChangeOnlyInCaseWithoutCheckingUsage() {
        when(repo.findById(5L)).thenReturn(Optional.of(attrezzatura("M-1")));
        when(repo.save(any())).thenAnswer(call -> call.getArgument(0));

        service.update(5L, new AttrezzaturaRequest("m-1", null, null, null, null));

        verify(noleggiRepo, never()).existsByMatricola(any());
    }

    @Test
    void rejectsAnUnknownStato() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(new AttrezzaturaRequest("M-3", null, null, null, "Noleggiata")));
        verify(repo, never()).save(any());
    }

    @Test
    void normalizesStatoAndBlankFields() {
        when(repo.save(any())).thenAnswer(call -> call.getArgument(0));

        service.create(new AttrezzaturaRequest(" M-3 ", "  ", "", " Marca ", " in_uso "));

        ArgumentCaptor<Attrezzature> saved = ArgumentCaptor.forClass(Attrezzature.class);
        verify(repo).save(saved.capture());
        assertEquals("M-3", saved.getValue().getMatricola());
        assertEquals("IN_USO", saved.getValue().getStato());
        assertEquals("Marca", saved.getValue().getMarca());
        assertNull(saved.getValue().getDescrizione());
        assertNull(saved.getValue().getModello());
    }

    @Test
    void defaultsAMissingStatoToDisponibile() {
        when(repo.save(any())).thenAnswer(call -> call.getArgument(0));

        service.create(new AttrezzaturaRequest("M-4", null, null, null, "  "));

        ArgumentCaptor<Attrezzature> saved = ArgumentCaptor.forClass(Attrezzature.class);
        verify(repo).save(saved.capture());
        assertEquals("DISPONIBILE", saved.getValue().getStato());
    }
}
