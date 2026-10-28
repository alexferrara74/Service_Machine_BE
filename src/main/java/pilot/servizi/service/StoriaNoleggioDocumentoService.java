package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pilot.servizi.dto.response.DocumentoNoleggioResponse;
import pilot.servizi.entity.StoriaNoleggioDocumento;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.StoriaNoleggioDocumentoRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoriaNoleggioDocumentoService {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

    private final StoriaNoleggioDocumentoRepository documentoRepository;
    private final StoriaNoleggiRepository noleggiRepository;

    @Transactional
    public DocumentoNoleggioResponse upload(Integer docId, MultipartFile file) {
        validateNoleggio(docId);
        if (file == null || file.isEmpty()) {
            throw invalidDocument("Il documento PDF è obbligatorio e non può essere vuoto.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw invalidDocument("Il documento supera la dimensione massima di 20 MB.");
        }

        String nomeFile = safeFileName(file.getOriginalFilename());
        if (!nomeFile.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            throw invalidDocument("È consentito caricare solo documenti PDF.");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new UncheckedIOException("Impossibile leggere il documento PDF caricato.", exception);
        }
        if (content.length < PDF_SIGNATURE.length ||
                !Arrays.equals(Arrays.copyOf(content, PDF_SIGNATURE.length), PDF_SIGNATURE)) {
            throw invalidDocument("Il file selezionato non è un documento PDF valido.");
        }

        StoriaNoleggioDocumento documento = documentoRepository.findById(docId)
                .orElseGet(StoriaNoleggioDocumento::new);
        documento.setDocId(docId);
        documento.setNomeFile(nomeFile);
        documento.setContentType("application/pdf");
        documento.setContenuto(content);
        StoriaNoleggioDocumento saved = documentoRepository.save(documento);
        return new DocumentoNoleggioResponse(saved.getNomeFile(), saved.getAggiornatoIl());
    }

    public DocumentoNoleggioResponse getMetadata(Integer docId) {
        validateNoleggio(docId);
        return documentoRepository.findById(docId)
                .map(documento -> new DocumentoNoleggioResponse(
                        documento.getNomeFile(),
                        documento.getAggiornatoIl()
                ))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento non presente per il noleggio " + docId
                ));
    }

    public DocumentoNoleggioFile download(Integer docId) {
        validateNoleggio(docId);
        return documentoRepository.findById(docId)
                .map(documento -> new DocumentoNoleggioFile(
                        documento.getNomeFile(),
                        documento.getContenuto()
                ))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento non presente per il noleggio " + docId
                ));
    }

    private void validateNoleggio(Integer docId) {
        if (docId == null || docId < 1) {
            throw invalidDocument("L'ID del noleggio deve essere positivo.");
        }
        if (!noleggiRepository.existsById(docId)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND,
                    "documentoNoleggio", "Noleggio non trovato: " + docId);
            throw new ResourceNotFoundException("Noleggio non trovato: " + docId);
        }
    }

    private String safeFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw invalidDocument("Il nome del documento è obbligatorio.");
        }
        String normalized = originalFilename.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (name.isEmpty() || name.length() > 255) {
            throw invalidDocument("Il nome del documento deve contenere al massimo 255 caratteri.");
        }
        return name;
    }

    private IllegalArgumentException invalidDocument(String message) {
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION,
                "documentoNoleggio", message);
        return new IllegalArgumentException(message);
    }

    public record DocumentoNoleggioFile(String nomeFile, byte[] contenuto) {}
}
