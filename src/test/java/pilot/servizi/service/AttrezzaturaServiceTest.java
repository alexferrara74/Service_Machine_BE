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
    @Mock
    private pilot.servizi.repository.ProdottiCatalogoRepository prodottiRepo;

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

    @Test
    void aNewAttrezzaturaIsMarkedAsManual() {
        when(repo.existsByMatricola("M-9")).thenReturn(false);
        when(repo.save(any())).thenAnswer(call -> call.getArgument(0));

        var r = service.create(new pilot.servizi.dto.request.AttrezzaturaRequest("M-9", "Idropulitrice", null, null, "DISPONIBILE"));

        assertEquals("MANUALE", r.origine());
    }

    // ---- collegamento all'articolo del catalogo -----------------------------------------------

    private pilot.servizi.entity.ProdottiCatalogo articolo(long id, boolean attrezzatura) {
        pilot.servizi.entity.ProdottiCatalogo p = new pilot.servizi.entity.ProdottiCatalogo();
        p.setArticoloId(id);
        p.setCodice("MAC-" + id);
        p.setIsAttrezzatura(attrezzatura);
        return p;
    }

    private pilot.servizi.dto.request.AttrezzaturaRequest conArticolo(Long articoloId) {
        return new pilot.servizi.dto.request.AttrezzaturaRequest("M-9", "Macchina", null, null, "DISPONIBILE", articoloId);
    }

    @Test
    void linksANewAttrezzaturaToACatalogArticle() {
        when(prodottiRepo.findById(7L)).thenReturn(Optional.of(articolo(7L, true)));
        when(repo.findByArticoloId(7L)).thenReturn(Optional.empty());
        when(repo.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> { Attrezzature e = i.getArgument(0); e.setId(9L); return e; });

        var r = service.create(conArticolo(7L));

        assertEquals(7L, r.articoloId());
    }

    @Test
    void rejectsAnUnknownArticleAndOneThatIsNotAnEquipment() {
        when(prodottiRepo.findById(8L)).thenReturn(Optional.empty());
        when(prodottiRepo.findById(7L)).thenReturn(Optional.of(articolo(7L, false)));

        assertThrows(pilot.servizi.log.ResourceNotFoundException.class, () -> service.create(conArticolo(8L)));
        assertThrows(IllegalArgumentException.class, () -> service.create(conArticolo(7L)));
        verify(repo, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void refusesAnArticleAlreadyLinkedToAnotherSerialNumber() {
        Attrezzature altra = attrezzatura("M-ALTRA");
        altra.setId(3L);
        when(prodottiRepo.findById(7L)).thenReturn(Optional.of(articolo(7L, true)));
        when(repo.findByArticoloId(7L)).thenReturn(Optional.of(altra));

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> service.create(conArticolo(7L)));
        verify(repo, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updatingWithoutAnArticleKeepsTheExistingLink() {
        Attrezzature e = attrezzatura("M-9");
        e.setArticoloId(7L); // per esempio il collegamento scritto dall'ERP
        when(repo.findById(5L)).thenReturn(Optional.of(e));
        when(repo.save(e)).thenReturn(e);

        service.update(5L, new pilot.servizi.dto.request.AttrezzaturaRequest("M-9", "Nuova descrizione", null, null, "DISPONIBILE"));

        assertEquals(7L, e.getArticoloId());
    }
}
