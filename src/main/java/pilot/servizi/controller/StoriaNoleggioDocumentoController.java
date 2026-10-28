package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pilot.servizi.dto.response.DocumentoNoleggioResponse;
import pilot.servizi.service.StoriaNoleggioDocumentoService;
import pilot.servizi.service.StoriaNoleggioDocumentoService.DocumentoNoleggioFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/storia/noleggi/{docId}/documento")
@Tag(name = "Documenti Noleggio", description = "Caricamento e download del contratto di noleggio in PDF")
@RequiredArgsConstructor
public class StoriaNoleggioDocumentoController {

    private final StoriaNoleggioDocumentoService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Carica o sostituisce il documento PDF del noleggio (massimo 20 MB)")
    public DocumentoNoleggioResponse upload(
            @PathVariable Integer docId,
            @RequestParam("file") MultipartFile file
    ) {
        return service.upload(docId, file);
    }

    @GetMapping
    @Operation(summary = "Legge i metadati del documento di noleggio")
    public DocumentoNoleggioResponse getMetadata(@PathVariable Integer docId) {
        return service.getMetadata(docId);
    }

    @GetMapping("/download")
    @Operation(summary = "Scarica il documento PDF del noleggio")
    public ResponseEntity<byte[]> download(@PathVariable Integer docId) {
        DocumentoNoleggioFile documento = service.download(docId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(documento.nomeFile(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(documento.contenuto());
    }
}
