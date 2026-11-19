package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pilot.servizi.dto.request.AppuntamentoRequest;
import pilot.servizi.dto.response.AppuntamentoResponse;
import pilot.servizi.service.NoleggioAppuntamentoService;

import java.util.List;

@RestController
@Tag(name = "Appuntamenti", description = "Appuntamenti di manutenzione con resoconto, foto ed esito")
@RequiredArgsConstructor
public class NoleggioAppuntamentoController {

    private final NoleggioAppuntamentoService service;

    @GetMapping("/api/appuntamenti")
    @Operation(summary = "Agenda di tutti gli appuntamenti, filtrabile per stato")
    public List<AppuntamentoResponse> agenda(@RequestParam(required = false) String stato) {
        return service.listAll(stato);
    }

    @GetMapping("/api/storia/noleggi/{docId}/appuntamenti")
    @Operation(summary = "Appuntamenti di un noleggio")
    public List<AppuntamentoResponse> list(@PathVariable Integer docId) {
        return service.list(docId);
    }

    @PostMapping("/api/storia/noleggi/{docId}/appuntamenti")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pianifica un appuntamento")
    public AppuntamentoResponse create(@PathVariable Integer docId, @Valid @RequestBody AppuntamentoRequest dto) {
        return service.create(docId, dto);
    }

    @GetMapping("/api/storia/noleggi/{docId}/appuntamenti/{id}")
    @Operation(summary = "Apre un appuntamento")
    public AppuntamentoResponse get(@PathVariable Integer docId, @PathVariable Long id) {
        return service.get(docId, id);
    }

    @PutMapping("/api/storia/noleggi/{docId}/appuntamenti/{id}")
    @Operation(summary = "Salva data, tipo e bozza del resoconto")
    public AppuntamentoResponse update(@PathVariable Integer docId, @PathVariable Long id,
                                       @Valid @RequestBody AppuntamentoRequest dto) {
        return service.update(docId, id, dto);
    }

    @PostMapping("/api/storia/noleggi/{docId}/appuntamenti/{id}/completa")
    @Operation(summary = "Chiude l'appuntamento: registra la manutenzione e, se positivo, allunga la scadenza")
    public AppuntamentoResponse completa(@PathVariable Integer docId, @PathVariable Long id,
                                         @Valid @RequestBody AppuntamentoRequest dto) {
        return service.completa(docId, id, dto);
    }

    @PostMapping("/api/storia/noleggi/{docId}/appuntamenti/{id}/annulla")
    @Operation(summary = "Annulla un appuntamento aperto")
    public AppuntamentoResponse annulla(@PathVariable Integer docId, @PathVariable Long id) {
        return service.annulla(docId, id);
    }

    @DeleteMapping("/api/storia/noleggi/{docId}/appuntamenti/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un appuntamento non completato")
    public void delete(@PathVariable Integer docId, @PathVariable Long id) {
        service.delete(docId, id);
    }

    @PostMapping(value = "/api/storia/noleggi/{docId}/appuntamenti/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Allega una foto (JPEG, PNG o WebP, max 10 MB)")
    public AppuntamentoResponse.Foto aggiungiFoto(@PathVariable Integer docId, @PathVariable Long id,
                                                  @RequestParam("file") MultipartFile file) {
        return service.aggiungiFoto(docId, id, file);
    }

    @GetMapping("/api/storia/noleggi/{docId}/appuntamenti/{id}/foto/{fotoId}")
    @Operation(summary = "Scarica una foto")
    public ResponseEntity<byte[]> foto(@PathVariable Integer docId, @PathVariable Long id, @PathVariable Long fotoId) {
        NoleggioAppuntamentoService.FotoFile f = service.scaricaFoto(docId, id, fotoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + f.nomeFile() + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofHours(1)).cachePrivate())
                .body(f.contenuto());
    }

    @DeleteMapping("/api/storia/noleggi/{docId}/appuntamenti/{id}/foto/{fotoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una foto")
    public void eliminaFoto(@PathVariable Integer docId, @PathVariable Long id, @PathVariable Long fotoId) {
        service.eliminaFoto(docId, id, fotoId);
    }
}
