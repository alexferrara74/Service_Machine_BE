package pilot.servizi.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pilot.servizi.dto.response.AppuntamentoResponse;
import pilot.servizi.entity.AppuntamentoAssistenza.Stato;
import pilot.servizi.service.AppuntamentoExportService.Formato;
import pilot.servizi.service.AppuntamentoExportService.Risultato;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppuntamentoExportServiceTest {

    private static final LocalDateTime DA = LocalDateTime.of(2026, 11, 16, 0, 0);
    private static final LocalDateTime A = LocalDateTime.of(2026, 11, 23, 0, 0);

    @Mock
    private AppuntamentoAssistenzaService appuntamenti;

    @InjectMocks
    private AppuntamentoExportService service;

    private AppuntamentoResponse riga(long id, Stato stato, String cliente, boolean censito) {
        return new AppuntamentoResponse(id, censito ? 1 : null, censito, null, cliente, "011", "Via Roma 1", "M-1",
                "Idropulitrice", "Revisione àèì", "note", stato,
                LocalDateTime.of(2026, 11, 19, 9, 0), LocalDateTime.of(2026, 11, 19, 10, 30), "Marco", java.util.List.of());
    }

    @Test
    void exportsAnExcelWithOneRowPerAppointmentAndASummarySheet() throws IOException {
        when(appuntamenti.getByIntervallo(DA, A)).thenReturn(List.of(
                riga(1, Stato.DA_FARE, "Rossi Srl", true), riga(2, Stato.ANNULLATO, "Bianchi", false)));

        Risultato r = service.esporta(DA, A, null, Formato.XLSX);

        assertEquals("assistenze_2026-11-16_2026-11-22.xlsx", r.nomeFile());
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(r.contenuto()))) {
            Sheet s = wb.getSheet("Appuntamenti");
            assertEquals("Assistenze dal 16/11/2026 al 22/11/2026", s.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Data", s.getRow(3).getCell(0).getStringCellValue());
            assertEquals("Rossi Srl", s.getRow(4).getCell(5).getStringCellValue());
            assertEquals("Da fare", s.getRow(4).getCell(4).getStringCellValue());
            assertEquals("Giovedì", s.getRow(4).getCell(1).getStringCellValue());
            assertEquals("No", s.getRow(5).getCell(13).getStringCellValue());
            assertEquals(5, s.getLastRowNum());
            assertEquals(2.0, wb.getSheet("Riepilogo").getRow(5).getCell(1).getNumericCellValue());
        }
    }

    @Test
    void appliesTheStatusFilter() throws IOException {
        when(appuntamenti.getByIntervallo(DA, A)).thenReturn(List.of(
                riga(1, Stato.DA_FARE, "Rossi Srl", true), riga(2, Stato.ANNULLATO, "Bianchi", false)));

        Risultato r = service.esporta(DA, A, Set.of(Stato.DA_FARE), Formato.XLSX);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(r.contenuto()))) {
            assertEquals(4, wb.getSheet("Appuntamenti").getLastRowNum());
        }
    }

    @Test
    void exportsAPdfEvenWhenThePeriodIsEmpty() {
        when(appuntamenti.getByIntervallo(DA, A)).thenReturn(List.of());
        Risultato vuoto = service.esporta(DA, A, null, Formato.PDF);
        assertTrue(new String(vuoto.contenuto(), 0, 5, StandardCharsets.ISO_8859_1).startsWith("%PDF"));

        when(appuntamenti.getByIntervallo(DA, A)).thenReturn(List.of(riga(1, Stato.FATTO, "Rossi Srl", true)));
        Risultato pieno = service.esporta(DA, A, null, Formato.PDF);
        assertTrue(pieno.contenuto().length > vuoto.contenuto().length);
        assertEquals("assistenze_2026-11-16_2026-11-22.pdf", pieno.nomeFile());
    }

    @Test
    void titlesASingleDayDifferently() {
        assertEquals("Assistenze del 19/11/2026",
                AppuntamentoExportService.titolo(LocalDateTime.of(2026, 11, 19, 0, 0), LocalDateTime.of(2026, 11, 20, 0, 0)));
    }

    @Test
    void rejectsAnUnknownFormat() {
        assertThrows(IllegalArgumentException.class, () -> Formato.from("doc"));
        assertEquals(Formato.PDF, Formato.from("PDF"));
    }
}
