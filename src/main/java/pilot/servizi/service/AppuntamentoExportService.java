package pilot.servizi.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.response.AppuntamentoResponse;
import pilot.servizi.entity.AppuntamentoAssistenza.Stato;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Esporta gli appuntamenti di un periodo (giorno, settimana, mese: lo decide l'intervallo) in Excel o PDF. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppuntamentoExportService {

    public enum Formato {
        XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        PDF("pdf", "application/pdf");

        public final String estensione;
        public final String contentType;

        Formato(String estensione, String contentType) {
            this.estensione = estensione;
            this.contentType = contentType;
        }

        public static Formato from(String value) {
            if (value != null) {
                for (Formato f : values()) {
                    if (f.estensione.equalsIgnoreCase(value.trim())) return f;
                }
            }
            throw new IllegalArgumentException("Formato non supportato: usare 'xlsx' o 'pdf'");
        }
    }

    private static final Locale IT = Locale.ITALIAN;
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_ORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Colore di riempimento e di testo per stato: stessa palette (token --st-*) del calendario a video. */
    private static final Map<Stato, int[][]> COLORI = new EnumMap<>(Stato.class);
    static {
        COLORI.put(Stato.DA_CONFERMARE, new int[][]{{0xFF, 0xF0, 0xCC}, {0xB2, 0x6A, 0x00}});
        COLORI.put(Stato.DA_FARE, new int[][]{{0xDD, 0xE9, 0xF7}, {0x1D, 0x5F, 0xA8}});
        COLORI.put(Stato.FATTO, new int[][]{{0xE0, 0xF2, 0xE8}, {0x1F, 0x7A, 0x4D}});
        COLORI.put(Stato.ANNULLATO, new int[][]{{0xEC, 0xE6, 0xE6}, {0x8A, 0x3A, 0x3A}});
    }

    private final AppuntamentoAssistenzaService appuntamenti;

    public record Risultato(byte[] contenuto, Formato formato, String nomeFile) {}

    /** {@code a} è escluso come nel calendario; {@code stati} vuoto o null = tutti gli stati. */
    public Risultato esporta(LocalDateTime da, LocalDateTime a, Collection<Stato> stati, Formato formato) {
        List<AppuntamentoResponse> righe = appuntamenti.getByIntervallo(da, a).stream()
                .filter(r -> stati == null || stati.isEmpty() || stati.contains(r.stato()))
                .toList();
        String titolo = titolo(da, a);
        String sottotitolo = stati == null || stati.isEmpty() || stati.size() == Stato.values().length
                ? null
                : "Stati: " + stati.stream().map(AppuntamentoExportService::etichetta).collect(Collectors.joining(", "));
        byte[] bytes = formato == Formato.XLSX ? excel(righe, titolo, sottotitolo) : pdf(righe, titolo, sottotitolo);
        String nome = "assistenze_" + da.toLocalDate() + "_" + a.toLocalDate().minusDays(1) + "." + formato.estensione;
        return new Risultato(bytes, formato, nome);
    }

    static String titolo(LocalDateTime da, LocalDateTime a) {
        LocalDate dal = da.toLocalDate();
        LocalDate al = a.toLocalDate().minusDays(1);
        if (!a.toLocalTime().equals(LocalTime.MIDNIGHT)) al = a.toLocalDate();
        return al.isAfter(dal)
                ? "Assistenze dal " + dal.format(DATA) + " al " + al.format(DATA)
                : "Assistenze del " + dal.format(DATA);
    }

    static String etichetta(Stato s) {
        return switch (s) {
            case DA_CONFERMARE -> "Da confermare";
            case DA_FARE -> "Da fare";
            case FATTO -> "Fatto";
            case ANNULLATO -> "Annullato";
        };
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String giorno(LocalDate d) {
        String g = d.getDayOfWeek().getDisplayName(TextStyle.FULL, IT);
        return Character.toUpperCase(g.charAt(0)) + g.substring(1);
    }

    // ===================== EXCEL =====================

    byte[] excel(List<AppuntamentoResponse> righe, String titolo, String sottotitolo) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CreationHelper ch = wb.getCreationHelper();
            XSSFFont bold = wb.createFont();
            bold.setBold(true);
            XSSFFont big = wb.createFont();
            big.setBold(true);
            big.setFontHeightInPoints((short) 14);
            XSSFFont headFont = wb.createFont();
            headFont.setBold(true);
            headFont.setColor(new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));

            XSSFCellStyle titoloStile = wb.createCellStyle();
            titoloStile.setFont(big);
            XSSFCellStyle head = wb.createCellStyle();
            head.setFont(headFont);
            head.setFillForegroundColor(new XSSFColor(new byte[]{0x12, 0x41, 0x4A}));
            head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFCellStyle testo = bordato(wb);
            testo.setWrapText(true);
            testo.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
            XSSFCellStyle data = bordato(wb);
            data.setDataFormat(ch.createDataFormat().getFormat("dd/mm/yyyy"));
            data.setAlignment(HorizontalAlignment.LEFT);
            XSSFCellStyle ora = bordato(wb);
            ora.setDataFormat(ch.createDataFormat().getFormat("hh:mm"));
            ora.setAlignment(HorizontalAlignment.LEFT);
            Map<Stato, XSSFCellStyle> perStato = new EnumMap<>(Stato.class);
            COLORI.forEach((stato, c) -> {
                XSSFCellStyle st = bordato(wb);
                st.setFillForegroundColor(rgb(c[0]));
                st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                XSSFFont f = wb.createFont();
                f.setBold(true);
                f.setColor(rgb(c[1]));
                st.setFont(f);
                perStato.put(stato, st);
            });

            Sheet sheet = wb.createSheet("Appuntamenti");
            sheet.createRow(0).createCell(0).setCellValue(titolo);
            sheet.getRow(0).getCell(0).setCellStyle(titoloStile);
            String sub = "Generato il " + LocalDateTime.now().format(DATA_ORA) + (sottotitolo != null ? " · " + sottotitolo : "");
            sheet.createRow(1).createCell(0).setCellValue(sub);

            String[] colonne = {"Data", "Giorno", "Dalle", "Alle", "Stato", "Cliente", "Telefono", "Matricola",
                    "Apparecchio", "Intervento", "Indirizzo", "Tecnico", "Note", "Cliente censito"};
            int headerRow = 3;
            Row h = sheet.createRow(headerRow);
            for (int i = 0; i < colonne.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(colonne[i]);
                c.setCellStyle(head);
            }
            int r = headerRow + 1;
            for (AppuntamentoResponse a : righe) {
                Row row = sheet.createRow(r++);
                cella(row, 0, data).setCellValue(a.inizio().toLocalDate());
                cella(row, 1, testo).setCellValue(giorno(a.inizio().toLocalDate()));
                cella(row, 2, ora).setCellValue(a.inizio().toLocalTime().atDate(LocalDate.of(1900, 1, 1)));
                cella(row, 3, ora).setCellValue(a.fine().toLocalTime().atDate(LocalDate.of(1900, 1, 1)));
                cella(row, 4, perStato.get(a.stato())).setCellValue(etichetta(a.stato()));
                cella(row, 5, testo).setCellValue(nz(a.cliente()));
                cella(row, 6, testo).setCellValue(nz(a.clienteTelefono()));
                cella(row, 7, testo).setCellValue(nz(a.matricola()));
                cella(row, 8, testo).setCellValue(nz(a.apparecchio()));
                cella(row, 9, testo).setCellValue(nz(a.titolo()));
                cella(row, 10, testo).setCellValue(nz(a.indirizzo()));
                cella(row, 11, testo).setCellValue(nz(a.tecnico()));
                cella(row, 12, testo).setCellValue(nz(a.note()));
                cella(row, 13, testo).setCellValue(a.censito() ? "Sì" : "No");
            }
            int[] larghezze = {12, 12, 8, 8, 15, 28, 16, 16, 24, 30, 28, 16, 40, 10};
            for (int i = 0; i < larghezze.length; i++) sheet.setColumnWidth(i, larghezze[i] * 256);
            sheet.createFreezePane(0, headerRow + 1);
            if (!righe.isEmpty()) {
                sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(headerRow, headerRow + righe.size(), 0, colonne.length - 1));
            }
            sheet.getPrintSetup().setLandscape(true);
            sheet.getPrintSetup().setFitWidth((short) 1);
            sheet.getPrintSetup().setFitHeight((short) 0);
            sheet.setFitToPage(true);

            Sheet riepilogo = wb.createSheet("Riepilogo");
            Row rh = riepilogo.createRow(0);
            cella(rh, 0, head).setCellValue("Stato");
            cella(rh, 1, head).setCellValue("Appuntamenti");
            Map<Stato, Long> conteggi = righe.stream().collect(Collectors.groupingBy(AppuntamentoResponse::stato, Collectors.counting()));
            int rr = 1;
            for (Stato s : Stato.values()) {
                Row row = riepilogo.createRow(rr++);
                cella(row, 0, perStato.get(s)).setCellValue(etichetta(s));
                cella(row, 1, testo).setCellValue(conteggi.getOrDefault(s, 0L));
            }
            Row tot = riepilogo.createRow(rr);
            Cell tl = tot.createCell(0);
            tl.setCellValue("Totale");
            Cell tv = tot.createCell(1);
            tv.setCellValue(righe.size());
            riepilogo.setColumnWidth(0, 18 * 256);
            riepilogo.setColumnWidth(1, 16 * 256);

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Cell cella(Row row, int col, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellStyle(style);
        return c;
    }

    private static XSSFCellStyle bordato(XSSFWorkbook wb) {
        XSSFCellStyle st = wb.createCellStyle();
        st.setBorderBottom(BorderStyle.THIN);
        st.setBorderTop(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);
        st.setBorderRight(BorderStyle.THIN);
        XSSFColor grigio = new XSSFColor(new byte[]{(byte) 0xD8, (byte) 0xDE, (byte) 0xDC});
        st.setBottomBorderColor(grigio);
        st.setTopBorderColor(grigio);
        st.setLeftBorderColor(grigio);
        st.setRightBorderColor(grigio);
        return st;
    }

    private static XSSFColor rgb(int[] c) {
        return new XSSFColor(new byte[]{(byte) c[0], (byte) c[1], (byte) c[2]});
    }

    // ===================== PDF =====================

    byte[] pdf(List<AppuntamentoResponse> righe, String titolo, String sottotitolo) {
        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 30, 36);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(doc, out);
            HeaderFooter footer = new HeaderFooter(new Phrase("Pagina ", font(8, false, Color.GRAY)), true);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setBorder(Rectangle.NO_BORDER);
            doc.setFooter(footer);
            doc.open();

            doc.add(new Paragraph(titolo, font(16, true, new Color(0x12, 0x41, 0x4A))));
            String sub = "Generato il " + LocalDateTime.now().format(DATA_ORA) + (sottotitolo != null ? " · " + sottotitolo : "");
            Paragraph p = new Paragraph(sub, font(9, false, Color.GRAY));
            p.setSpacingAfter(4);
            doc.add(p);

            Map<Stato, Long> conteggi = righe.stream().collect(Collectors.groupingBy(AppuntamentoResponse::stato, Collectors.counting()));
            String riepilogo = java.util.Arrays.stream(Stato.values())
                    .map(s -> etichetta(s) + ": " + conteggi.getOrDefault(s, 0L))
                    .collect(Collectors.joining("   ·   ")) + "   ·   Totale: " + righe.size();
            Paragraph rp = new Paragraph(riepilogo, font(9, true, Color.DARK_GRAY));
            rp.setSpacingAfter(10);
            doc.add(rp);

            if (righe.isEmpty()) {
                doc.add(new Paragraph("Nessun appuntamento nel periodo selezionato.", font(11, false, Color.DARK_GRAY)));
            } else {
                PdfPTable t = new PdfPTable(new float[]{1.3f, 1.6f, 2.4f, 2.8f, 2.2f, 1.3f, 2.5f});
                t.setWidthPercentage(100);
                t.setHeaderRows(1);
                for (String h : new String[]{"Orario", "Stato", "Cliente", "Intervento", "Apparecchio / matricola", "Tecnico", "Contatti e note"}) {
                    PdfPCell c = new PdfPCell(new Phrase(h, font(9, true, Color.WHITE)));
                    c.setBackgroundColor(new Color(0x12, 0x41, 0x4A));
                    c.setPadding(5);
                    t.addCell(c);
                }
                LocalDate corrente = null;
                for (AppuntamentoResponse a : righe) {
                    LocalDate d = a.inizio().toLocalDate();
                    if (!d.equals(corrente)) {
                        corrente = d;
                        PdfPCell g = new PdfPCell(new Phrase(giorno(d) + " " + d.format(DATA), font(10, true, new Color(0x12, 0x41, 0x4A))));
                        g.setColspan(7);
                        g.setBackgroundColor(new Color(0xED, 0xF0, 0xEE));
                        g.setPadding(5);
                        t.addCell(g);
                    }
                    int[][] col = COLORI.get(a.stato());
                    Color testoStato = new Color(col[1][0], col[1][1], col[1][2]);
                    PdfPCell stato = cellaPdf(etichetta(a.stato()), font(9, true, testoStato));
                    stato.setBackgroundColor(new Color(col[0][0], col[0][1], col[0][2]));
                    String orario = a.inizio().toLocalTime().format(ORA) + "–" + a.fine().toLocalTime().format(ORA);
                    t.addCell(cellaPdf(orario, font(9, true, Color.BLACK)));
                    t.addCell(stato);
                    t.addCell(cellaPdf(nz(a.cliente()) + (a.censito() ? "" : "\n(non censito)"), font(9, false, Color.BLACK)));
                    t.addCell(cellaPdf(nz(a.titolo()), font(9, false, Color.BLACK)));
                    String app = java.util.stream.Stream.of(a.apparecchio(), a.matricola())
                            .filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining("\n"));
                    t.addCell(cellaPdf(app, font(9, false, Color.BLACK)));
                    t.addCell(cellaPdf(nz(a.tecnico()), font(9, false, Color.BLACK)));
                    String contatti = java.util.stream.Stream.of(a.clienteTelefono(), a.indirizzo(), a.note())
                            .filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining("\n"));
                    t.addCell(cellaPdf(contatti, font(8, false, Color.DARK_GRAY)));
                }
                doc.add(t);
            }
            doc.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Generazione PDF non riuscita", e);
        }
    }

    private static PdfPCell cellaPdf(String testo, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(testo, font));
        c.setPadding(4);
        c.setBorderColor(new Color(0xD8, 0xDE, 0xDC));
        return c;
    }

    private static Font font(float size, boolean bold, Color color) {
        return FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, size, Font.NORMAL, color);
    }
}
