package pilot.servizi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pilot.servizi.dto.request.NoleggioManutenzioneRequest;
import pilot.servizi.dto.request.NoleggioManutenzioneConfigurazioneRequest;
import pilot.servizi.dto.request.NoleggioManutenzionePianificazioneRequest;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.ClienteManutenzionePeriodo;
import pilot.servizi.entity.ClienteManutenzionePiano;
import pilot.servizi.entity.NoleggioManutenzione;
import pilot.servizi.entity.NoleggioManutenzioneConfigurazione;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.ClienteManutenzionePeriodoRepository;
import pilot.servizi.repository.ClienteManutenzionePianoRepository;
import pilot.servizi.repository.NoleggioManutenzioneConfigurazioneRepository;
import pilot.servizi.repository.NoleggioManutenzioneRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;
import pilot.servizi.repository.StoriaProdottiRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoleggioManutenzioneServiceTest {

    @Mock
    private StoriaNoleggiRepository noleggiRepo;
    @Mock
    private ClientiRepository clientiRepo;
    @Mock
    private NoleggioManutenzioneConfigurazioneRepository configurazioniRepo;
    @Mock
    private ClienteManutenzionePianoRepository pianiClienteRepo;
    @Mock
    private ClienteManutenzionePeriodoRepository periodiClienteRepo;
    @Mock
    private NoleggioManutenzioneRepository manutenzioniRepo;
    @Mock
    private StoriaProdottiRepository prodottiRepo;

    private NoleggioManutenzioneService service;
    private final List<NoleggioManutenzione> eventi = new ArrayList<>();
    private final List<ClienteManutenzionePeriodo> configuredPeriods = new ArrayList<>();
    private double quantitaAcquistata;

    @BeforeEach
    void setUp() {
        service = new NoleggioManutenzioneService(
                noleggiRepo,
                clientiRepo,
                configurazioniRepo,
                pianiClienteRepo,
                periodiClienteRepo,
                manutenzioniRepo,
                prodottiRepo
        );

        StoriaNoleggi noleggio = StoriaNoleggi.builder()
                .docId(42)
                .dataDoc(LocalDate.now().minusDays(40))
                .clienteId(7L)
                .articoloId(15L)
                .matricola("MAC-42")
                .quantita(1d)
                .build();
        Clienti cliente = Clienti.builder()
                .id(7L)
                .ragioneSociale("Cliente Test")
                .build();
        NoleggioManutenzioneConfigurazione config = NoleggioManutenzioneConfigurazione.builder()
                .docId(42)
                .sogliaQuantita(10d)
                .ordinariePerStraordinaria(2)
                .categoriaIds(Set.of(4, 7))
                .build();

        lenient().when(noleggiRepo.findById(42)).thenReturn(Optional.of(noleggio));
        lenient().when(noleggiRepo.findById(43)).thenReturn(Optional.of(StoriaNoleggi.builder()
                .docId(43)
                .dataDoc(LocalDate.now().minusDays(20))
                .clienteId(7L)
                .articoloId(16L)
                .matricola("MAC-43")
                .quantita(1d)
                .build()));
        lenient().when(clientiRepo.findById(7L)).thenReturn(Optional.of(cliente));
        lenient().when(configurazioniRepo.findById(42)).thenReturn(Optional.of(config));
        lenient().when(pianiClienteRepo.findById(7L)).thenReturn(Optional.of(ClienteManutenzionePiano.builder()
                .clienteId(7L).giorniAvviso(7).build()));
        lenient().when(periodiClienteRepo.findByClienteIdOrderByDataInizioAsc(7L))
                .thenAnswer(invocation -> List.copyOf(configuredPeriods));
        lenient().when(pianiClienteRepo.save(any(ClienteManutenzionePiano.class))).thenAnswer(invocation -> {
            ClienteManutenzionePiano saved = invocation.getArgument(0);
            when(pianiClienteRepo.findById(7L)).thenReturn(Optional.of(saved));
            return saved;
        });
        lenient().when(periodiClienteRepo.saveAll(anyList())).thenAnswer(invocation -> {
            configuredPeriods.clear();
            configuredPeriods.addAll(invocation.getArgument(0));
            return invocation.getArgument(0);
        });
        lenient().doAnswer(invocation -> {
            configuredPeriods.clear();
            return null;
        }).when(periodiClienteRepo).deleteByClienteId(7L);
        lenient().when(manutenzioniRepo.findByDocIdOrderByIdAsc(42)).thenAnswer(invocation -> List.copyOf(eventi));
        lenient().when(manutenzioniRepo.findFirstByDocIdAndTipoOrderByIdDesc(
                42, NoleggioManutenzione.Tipo.ORDINARIA
        )).thenAnswer(invocation -> eventi.stream()
                .filter(evento -> evento.getTipo() == NoleggioManutenzione.Tipo.ORDINARIA)
                .reduce((first, second) -> second));
        lenient().when(manutenzioniRepo.findFirstByDocIdAndTipoOrderByIdDesc(
                42, NoleggioManutenzione.Tipo.STRAORDINARIA
        )).thenAnswer(invocation -> eventi.stream()
                .filter(evento -> evento.getTipo() == NoleggioManutenzione.Tipo.STRAORDINARIA)
                .reduce((first, second) -> second));
        lenient().when(manutenzioniRepo.countByDocIdAndTipoAndIdGreaterThan(
                org.mockito.ArgumentMatchers.eq(42),
                org.mockito.ArgumentMatchers.eq(NoleggioManutenzione.Tipo.ORDINARIA),
                anyLong()
        )).thenAnswer(invocation -> {
            Long afterId = invocation.getArgument(2);
            return eventi.stream()
                    .filter(evento -> evento.getTipo() == NoleggioManutenzione.Tipo.ORDINARIA)
                    .filter(evento -> evento.getId() > afterId)
                    .count();
        });
        lenient().when(prodottiRepo.sumQuantitaByClienteIdAndCategoriaIdIn(7L, List.of(4, 7)))
                .thenAnswer(invocation -> quantitaAcquistata);
        configuredPeriods.add(ClienteManutenzionePeriodo.builder()
                .clienteId(7L)
                .dataInizio(LocalDate.now().minusDays(365))
                .dataFine(LocalDate.now().plusDays(365))
                .intervalloGiorni(30)
                .build());
        lenient().when(manutenzioniRepo.save(any(NoleggioManutenzione.class))).thenAnswer(invocation -> {
            NoleggioManutenzione evento = invocation.getArgument(0);
            evento.setId((long) eventi.size() + 1);
            eventi.add(evento);
            return evento;
        });
    }

    @Test
    void ordinaryMaintenanceDueDateUsesCalendarAndLastOrdinaryEvent() {
        var stato = service.getStato(42);
        assertTrue(stato.manutenzioneDovuta());
        assertEquals("ORDINARIA", stato.tipoManutenzioneDovuta());
        assertEquals("IN_RITARDO", stato.statoManutenzione());

        var evento = service.registraManutenzione(42, new NoleggioManutenzioneRequest("manutenzione ordinaria"));
        assertEquals("ORDINARIA", evento.tipo());
        assertEquals(LocalDateTime.now().toLocalDate().plusDays(30), service.getStato(42).prossimaManutenzioneIl());
        assertEquals(1, eventi.size());
    }

    @Test
    void extraordinaryMaintenanceCanBeRegisteredWithoutChangingOrdinarySchedule() {
        var evento = service.registraStraordinaria(42, new NoleggioManutenzioneRequest("guasto"));

        assertEquals("STRAORDINARIA", evento.tipo());
        assertTrue(service.getStato(42).manutenzioneDovuta());
    }

    @Test
    void customerScheduleIsAppliedToEveryRentalWhileEventsRemainPerRental() {
        assertEquals(30, service.getStato(42).intervalloManutenzioneGiorni());
        assertEquals(30, service.getStato(43).intervalloManutenzioneGiorni());
        verify(pianiClienteRepo, org.mockito.Mockito.times(2)).findById(7L);
    }

    @Test
    void configuresSeasonalPeriodsForCustomer() {
        var request = new NoleggioManutenzionePianificazioneRequest(
                10,
                List.of(
                        new NoleggioManutenzionePianificazioneRequest.PeriodoRequest(
                                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31), 30
                        ),
                        new NoleggioManutenzionePianificazioneRequest.PeriodoRequest(
                                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 5, 31), 90
                        )
                )
        );

        var result = service.configuraPianificazioneCliente(7L, request);

        assertEquals(10, result.giorniAvviso());
        assertEquals(2, result.periodi().size());
    }

    @Test
    void savesMaintenanceCategoriesInMutableCollection() {
        var request = new NoleggioManutenzioneConfigurazioneRequest(12d, 3, Set.of(2, 5));

        service.configura(42, request);

        ArgumentCaptor<NoleggioManutenzioneConfigurazione> captor =
                ArgumentCaptor.forClass(NoleggioManutenzioneConfigurazione.class);
        verify(configurazioniRepo).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(Set.of(2, 5), saved.getCategoriaIds());
        saved.getCategoriaIds().add(8);
        assertTrue(saved.getCategoriaIds().contains(8));
    }
}
