package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pilot.servizi.dto.request.PosizioneArticoloRequest;
import pilot.servizi.dto.request.RicambioRequest;
import pilot.servizi.dto.response.RicambioResponse;
import pilot.servizi.entity.MagazzinoDigitale;
import pilot.servizi.entity.ProdottiCatalogo;
import pilot.servizi.entity.Ricambi;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.InterventiRepository;
import pilot.servizi.repository.MagazzinoDigitaleRepository;
import pilot.servizi.repository.MagazzinoPosizioniArticoliRepository;
import pilot.servizi.repository.ProdottiCatalogoRepository;
import pilot.servizi.repository.RicambisticaInterventiRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Ricambi e giacenze devono puntare a un articolo del catalogo, non a un ID qualsiasi (es. il 0 del form). */
@ExtendWith(MockitoExtension.class)
class ArticoloCatalogoValidationTest {

    @Mock
    private RicambisticaInterventiRepository ricambiRepo;
    @Mock
    private InterventiRepository interventiRepo;
    @Mock
    private ProdottiCatalogoRepository catalogoRepo;
    @Mock
    private MagazzinoPosizioniArticoliRepository posizioniRepo;
    @Mock
    private MagazzinoDigitaleRepository magazzinoRepo;

    @InjectMocks
    private RicambisticaInterventiService ricambiService;
    @InjectMocks
    private MagazzinoPosizioniArticoliService posizioniService;

    @Test
    void refusesASpareForAnArticleMissingFromTheCatalog() {
        when(interventiRepo.existsById(1L)).thenReturn(true);
        when(catalogoRepo.findById(0L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ricambiService.create(new RicambioRequest(1L, 0L, null, null, 1, null)));
        verify(ricambiRepo, never()).save(any());
    }

    @Test
    void fillsCodeAndDescriptionOfASpareFromTheCatalog() {
        ProdottiCatalogo articolo = new ProdottiCatalogo();
        articolo.setCodice("GUAR-01");
        articolo.setDescrizione("Guarnizione");
        when(interventiRepo.existsById(1L)).thenReturn(true);
        when(catalogoRepo.findById(9L)).thenReturn(Optional.of(articolo));
        when(ricambiRepo.save(any())).thenAnswer(call -> call.<Ricambi>getArgument(0));

        RicambioResponse saved = ricambiService.create(new RicambioRequest(1L, 9L, null, null, 2, null));

        assertEquals("GUAR-01", saved.codiceRicambio());
        assertEquals("Guarnizione", saved.descrizioneRicambio());
    }

    @Test
    void refusesStockForAnArticleMissingFromTheCatalog() {
        when(magazzinoRepo.findById(2L)).thenReturn(Optional.of(new MagazzinoDigitale()));
        when(catalogoRepo.existsById(0L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> posizioniService.create(new PosizioneArticoloRequest(2L, 0L, 5)));
        verify(posizioniRepo, never()).save(any());
    }
}
