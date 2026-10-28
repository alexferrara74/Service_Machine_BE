package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import pilot.servizi.entity.StoriaNoleggioDocumento;
import pilot.servizi.repository.StoriaNoleggioDocumentoRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoriaNoleggioDocumentoServiceTest {

    @Mock
    private StoriaNoleggioDocumentoRepository documentoRepository;

    @Mock
    private StoriaNoleggiRepository noleggiRepository;

    @InjectMocks
    private StoriaNoleggioDocumentoService service;

    @Test
    void uploadsPdfForAnExistingRental() {
        when(noleggiRepository.existsById(12)).thenReturn(true);
        when(documentoRepository.findById(12)).thenReturn(Optional.empty());
        when(documentoRepository.save(any(StoriaNoleggioDocumento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contratto.pdf",
                "application/pdf",
                "%PDF-1.7 contenuto".getBytes(java.nio.charset.StandardCharsets.US_ASCII)
        );

        var metadata = service.upload(12, file);

        assertEquals("contratto.pdf", metadata.nomeFile());
        verify(documentoRepository).save(any(StoriaNoleggioDocumento.class));
    }

    @Test
    void replacesTheExistingPdfInsteadOfCreatingAnotherDocument() {
        StoriaNoleggioDocumento existing = new StoriaNoleggioDocumento();
        existing.setDocId(12);
        existing.setNomeFile("precedente.pdf");
        existing.setContenuto("%PDF-1.4 vecchio".getBytes());
        when(noleggiRepository.existsById(12)).thenReturn(true);
        when(documentoRepository.findById(12)).thenReturn(Optional.of(existing));
        when(documentoRepository.save(any(StoriaNoleggioDocumento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nuovo.pdf",
                "application/pdf",
                "%PDF-1.7 nuovo".getBytes(java.nio.charset.StandardCharsets.US_ASCII)
        );

        var metadata = service.upload(12, file);

        assertEquals("nuovo.pdf", metadata.nomeFile());
        assertEquals("%PDF-1.7 nuovo", new String(existing.getContenuto(), java.nio.charset.StandardCharsets.US_ASCII));
        verify(documentoRepository).save(existing);
    }

    @Test
    void rejectsAFileThatDoesNotContainPdfData() {
        when(noleggiRepository.existsById(12)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile(
                "file", "contratto.pdf", "application/pdf", "not a pdf".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> service.upload(12, file));
        verify(documentoRepository, never()).save(any(StoriaNoleggioDocumento.class));
    }
}
