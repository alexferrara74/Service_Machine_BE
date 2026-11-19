package pilot.servizi.log;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import pilot.servizi.dto.response.ErrorResponse;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, request.getMethod() + " " + request.getRequestURI(), ex.getMessage());
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ErrorConstants.ERR_NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, request.getMethod() + " " + request.getRequestURI(), ex.getMessage());
        // Le violazioni lanciate dai service non hanno causa e hanno un messaggio per l'utente;
        // quelle del database (con causa) contengono SQL e nomi di vincoli e non vanno esposte.
        String message = ex.getCause() == null
                ? ex.getMessage()
                : "L'operazione viola un vincolo di integrità dei dati";
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ErrorConstants.ERR_CONFLICT,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, request.getMethod() + " " + request.getRequestURI(), ex.getMessage());
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorConstants.ERR_VALIDATION,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Errore di validazione DTO");

        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, request.getMethod() + " " + request.getRequestURI(), details);
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorConstants.ERR_VALIDATION,
                details,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String details = "Il documento supera la dimensione massima consentita di 20 MB.";
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION,
                request.getMethod() + " " + request.getRequestURI(), details);
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                ErrorConstants.ERR_VALIDATION,
                details,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
    }

    /** Credenziali errate dal login: sempre lo stesso messaggio, senza dire se esiste lo username. */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                ErrorConstants.AUTH_CREDENZIALI_NON_VALIDE,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String details = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                details,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception ex, HttpServletRequest request) {
        String details = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? "Valore non valido per il parametro '" + mismatch.getName() + "'"
                : invalidField(ex)
                        .map(field -> "Valore non valido per il campo '" + field + "'")
                        .orElse("Corpo della richiesta non leggibile o in formato non valido");
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, request.getMethod() + " " + request.getRequestURI(), details);
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorConstants.ERR_VALIDATION,
                details,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private static Optional<String> invalidField(Exception ex) {
        if (ex.getCause() instanceof MismatchedInputException mismatch) {
            return mismatch.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .reduce((first, last) -> last);
        }
        return Optional.empty();
    }

    /** Eccezioni MVC con stato proprio (404 risorsa/route, 405, 415, parametro mancante...): non sono errori del server. */
    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            ServletRequestBindingException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleFrameworkError(Exception exception, HttpServletRequest request) {
        org.springframework.web.ErrorResponse ex = (org.springframework.web.ErrorResponse) exception;
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String details = switch (status) {
            case NOT_FOUND -> "Risorsa non trovata";
            case METHOD_NOT_ALLOWED -> "Metodo HTTP non supportato per questo percorso";
            case UNSUPPORTED_MEDIA_TYPE -> "Formato del contenuto non supportato";
            case NOT_ACCEPTABLE -> "Formato di risposta non disponibile";
            default -> ex.getBody().getDetail() != null ? ex.getBody().getDetail() : status.getReasonPhrase();
        };
        log.warn(ErrorConstants.LOG_VALIDATION, status.name(), request.getMethod() + " " + request.getRequestURI(), details);
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.name(),
                details,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error(ErrorConstants.LOG_UNEXPECTED, ErrorConstants.ERR_INTERNAL, request.getMethod() + " " + request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorConstants.ERR_INTERNAL,
                "Si è verificato un errore interno nel server",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
