package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pilot.servizi.dto.request.AppuntamentoRequest;
import pilot.servizi.dto.request.AppuntamentoStatoRequest;
import pilot.servizi.dto.response.AppuntamentoResponse;
import pilot.servizi.entity.AppuntamentoAssistenza;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.service.AppuntamentoAssistenzaService;
import pilot.servizi.service.AppuntamentoExportService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appuntamenti")
@Tag(name = "Appuntamenti", description = "Calendario delle assistenze su noleggi censiti o clienti non censiti")
@RequiredArgsConstructor
public class AppuntamentoController {

    private final AppuntamentoAssistenzaService service;
    private final AppuntamentoExportService exportService;

    @GetMapping
    @Operation(summary = "Appuntamenti nell'intervallo [da, a) per il calendario")
    public List<AppuntamentoResponse> getByIntervallo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime da,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime a
    ) {
        return service.getByIntervallo(da, a);
    }

    @GetMapping("/export")
    @Operation(summary = "Esporta in Excel (xlsx) o PDF gli appuntamenti dell'intervallo [da, a): giorno, settimana o mese")
    public ResponseEntity<byte[]> esporta(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime da,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime a,
            @RequestParam(defaultValue = "xlsx") String formato,
            @RequestParam(required = false) List<AppuntamentoAssistenza.Stato> stati
    ) {
        AppuntamentoExportService.Risultato r = exportService.esporta(da, a, stati, AppuntamentoExportService.Formato.from(formato));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(r.formato().contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(r.nomeFile()).build().toString())
                .body(r.contenuto());
    }

    @GetMapping("/noleggio/{docId}")
    @Operation(summary = "Appuntamenti di un noleggio censito")
    public List<AppuntamentoResponse> getByNoleggio(@PathVariable Integer docId) {
        return service.getByNoleggio(docId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio appuntamento")
    public AppuntamentoResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Crea un appuntamento")
    @ResponseStatus(HttpStatus.CREATED)
    public AppuntamentoResponse create(@Valid @RequestBody AppuntamentoRequest dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna un appuntamento")
    public AppuntamentoResponse update(@PathVariable Long id, @Valid @RequestBody AppuntamentoRequest dto, Authentication auth) {
        nonAnnullaSeTecnico(auth, dto.stato());
        return service.update(id, dto);
    }

    @PatchMapping("/{id}/stato")
    @Operation(summary = "Cambia lo stato (da confermare, da fare, fatto, annullato)")
    public AppuntamentoResponse updateStato(@PathVariable Long id, @Valid @RequestBody AppuntamentoStatoRequest dto, Authentication auth) {
        nonAnnullaSeTecnico(auth, dto.stato());
        return service.updateStato(id, dto.stato());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un appuntamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Allega una foto all'appuntamento (JPEG, PNG o WebP, max 10 MB, fino a 20 foto)")
    public AppuntamentoResponse.Foto aggiungiFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return service.aggiungiFoto(id, file);
    }

    @GetMapping("/{id}/foto/{fotoId}")
    @Operation(summary = "Scarica una foto dell'appuntamento")
    public ResponseEntity<byte[]> foto(@PathVariable Long id, @PathVariable Long fotoId) {
        AppuntamentoAssistenzaService.FotoFile f = service.scaricaFoto(id, fotoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + f.nomeFile() + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofHours(1)).cachePrivate())
                .body(f.contenuto());
    }

    @DeleteMapping("/{id}/foto/{fotoId}")
    @Operation(summary = "Elimina una foto dell'appuntamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminaFoto(@PathVariable Long id, @PathVariable Long fotoId) {
        service.eliminaFoto(id, fotoId);
    }

    /** Il tecnico conferma e chiude gli appuntamenti ma non li annulla: è una decisione dell'ufficio. */
    private static void nonAnnullaSeTecnico(Authentication auth, AppuntamentoAssistenza.Stato stato) {
        boolean soloTecnico = auth != null && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (soloTecnico && stato == AppuntamentoAssistenza.Stato.ANNULLATO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorConstants.AUTH_NON_AUTORIZZATO);
        }
    }
}
