package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pilot.servizi.dto.request.ProdottoCatalogoRequest;
import pilot.servizi.dto.response.ProdottoCatalogoResponse;
import pilot.servizi.entity.ProdottiCatalogo;
import pilot.servizi.repository.ProdottiCatalogoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdottiCatalogoServiceTest {

    @Mock
    private ProdottiCatalogoRepository repo;

    @InjectMocks
    private ProdottiCatalogoService service;

    @Test
    void createsAManualAttrezzaturaArticleInTheManualIdRange() {
        when(repo.existsByCodiceIgnoreCase("TG-1")).thenReturn(false);
        when(repo.maxArticoloId()).thenReturn(52_000L);
        when(repo.save(any())).thenAnswer(call -> call.getArgument(0));

        ProdottoCatalogoResponse r = service.create(new ProdottoCatalogoRequest("  TG-1 ", " Tagliaerba ", null));

        ArgumentCaptor<ProdottiCatalogo> saved = ArgumentCaptor.forClass(ProdottiCatalogo.class);
        verify(repo).save(saved.capture());
        assertEquals(1_000_000_000L, saved.getValue().getArticoloId());
        assertEquals("TG-1", saved.getValue().getCodice());
        assertEquals("Tagliaerba", saved.getValue().getDescrizione());
        assertTrue(saved.getValue().getIsAttrezzatura());
        assertFalse(saved.getValue().getIsAccessorio());
        assertEquals("MANUALE", r.origine());
    }

    @Test
    void refusesADuplicateCode() {
        when(repo.existsByCodiceIgnoreCase("TG-1")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.create(new ProdottoCatalogoRequest("TG-1", "Altro", true)));
        verify(repo, never()).save(any());
    }

    @Test
    void requiresCodeAndDescription() {
        assertThrows(IllegalArgumentException.class, () -> service.create(new ProdottoCatalogoRequest("  ", "x", true)));
        assertThrows(IllegalArgumentException.class, () -> service.create(new ProdottoCatalogoRequest("C", " ", true)));
        assertThrows(IllegalArgumentException.class, () -> service.create(null));
        verify(repo, never()).save(any());
    }
}
