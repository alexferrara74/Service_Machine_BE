package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pilot.servizi.dto.request.ClienteRequest;
import pilot.servizi.entity.Clienti;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.InterventiRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;
import pilot.servizi.repository.StoriaProdottiRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientiServiceTest {

    @Mock
    private ClientiRepository repo;
    @Mock
    private StoriaNoleggiRepository noleggiRepo;
    @Mock
    private InterventiRepository interventiRepo;
    @Mock
    private StoriaProdottiRepository prodottiRepo;

    @InjectMocks
    private ClientiService service;

    @Test
    void deletesAClienteWithoutDependentData() {
        when(repo.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(repo).deleteById(7L);
    }

    @Test
    void refusesToDeleteAClienteWithRentals() {
        when(repo.existsById(7L)).thenReturn(true);
        when(noleggiRepo.existsByClienteId(7L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.delete(7L));
        verify(repo, never()).deleteById(7L);
    }

    @Test
    void refusesToDeleteAClienteWithInterventions() {
        when(repo.existsById(7L)).thenReturn(true);
        when(interventiRepo.existsByClienteId(7L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.delete(7L));
        verify(repo, never()).deleteById(7L);
    }

    @Test
    void refusesToDeleteAClienteWithPurchaseHistory() {
        when(repo.existsById(7L)).thenReturn(true);
        when(prodottiRepo.existsByClienteId(7L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.delete(7L));
        verify(repo, never()).deleteById(7L);
    }

    @Test
    void reportsAMissingClienteOnDelete() {
        when(repo.existsById(7L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(7L));
    }

    @Test
    void storesEmptyOptionalFieldsAsNullAndTrimsTheOthers() {
        when(repo.save(any())).thenAnswer(call -> call.getArgument(0));

        service.create(new ClienteRequest("C1", "Rossi Srl", "  Mario  ", "", "  ", "", "", null, null));

        ArgumentCaptor<Clienti> saved = ArgumentCaptor.forClass(Clienti.class);
        verify(repo).save(saved.capture());
        assertEquals("Mario", saved.getValue().getNome());
        assertNull(saved.getValue().getCognome());
        assertNull(saved.getValue().getTelefono());
        assertNull(saved.getValue().getEmail());
        assertNull(saved.getValue().getPartitaIva());
    }
}
