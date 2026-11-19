package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import pilot.servizi.dto.request.ProdottoCatalogoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.response.ProdottoCatalogoResponse;
import pilot.servizi.entity.ProdottiCatalogo;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ProdottiCatalogoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProdottiCatalogoService {

    private final ProdottiCatalogoRepository repository;

    /**
     * Con attrezzatura=false restituisce solo prodotti veri: gli accessori (is_accessorio)
     * si ottengono esplicitamente con accessorio=true.
     */
    public List<ProdottoCatalogoResponse> getAll(Integer categoriaId, Boolean attrezzatura, Boolean accessorio) {
        Boolean accessorioFilter = accessorio;
        if (accessorioFilter == null && Boolean.FALSE.equals(attrezzatura)) {
            accessorioFilter = false;
        }
        return repository.search(categoriaId, attrezzatura, accessorioFilter).stream()
                .map(this::toResponse)
                .toList();
    }

    public ProdottoCatalogoResponse getById(Long articoloId) {
        return repository.findById(articoloId)
                .map(this::toResponse)
                .orElseThrow(() -> {
                    String message = "Prodotto catalogo non trovato: " + articoloId;
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", message);
                    return new ResourceNotFoundException(message);
                });
    }

    /** Crea un articolo a mano (origine MANUALE): serve a registrare attrezzature che il catalogo ERP non contiene. */
    @Transactional
    public ProdottoCatalogoResponse create(ProdottoCatalogoRequest dto) {
        if (dto == null) {
            throw invalid(ErrorConstants.CATALOGO_DTO_NULL);
        }
        if (dto.codice() == null || dto.codice().trim().isEmpty()) {
            throw invalid(ErrorConstants.CATALOGO_CODICE_OBBLIGATORIO);
        }
        if (dto.codice().trim().length() > 50) {
            throw invalid(ErrorConstants.CATALOGO_CODICE_TOO_LONG);
        }
        if (dto.descrizione() == null || dto.descrizione().trim().isEmpty()) {
            throw invalid(ErrorConstants.CATALOGO_DESCRIZIONE_OBBLIGATORIA);
        }
        if (dto.descrizione().trim().length() > 255) {
            throw invalid(ErrorConstants.CATALOGO_DESCRIZIONE_TOO_LONG);
        }
        String codice = dto.codice().trim();
        if (repository.existsByCodiceIgnoreCase(codice)) {
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "create", ErrorConstants.CATALOGO_CODICE_DUPLICATO + codice);
            throw new DataIntegrityViolationException(ErrorConstants.CATALOGO_CODICE_DUPLICATO + codice);
        }
        ProdottiCatalogo p = new ProdottiCatalogo();
        p.setArticoloId(Origini.prossimoIdManuale(repository.maxArticoloId()));
        p.setCodice(codice);
        p.setDescrizione(dto.descrizione().trim());
        p.setIsAttrezzatura(dto.isAttrezzatura() == null || dto.isAttrezzatura());
        p.setIsAccessorio(false);
        p.setOrigine(Origini.MANUALE);
        return toResponse(repository.save(p));
    }

    private IllegalArgumentException invalid(String message) {
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "create", message);
        return new IllegalArgumentException(message);
    }

    private ProdottoCatalogoResponse toResponse(ProdottiCatalogo product) {
        return new ProdottoCatalogoResponse(
                product.getArticoloId(),
                product.getCodice(),
                product.getDescrizione(),
                product.getCategoriaId(),
                product.getCategoriaDescrizione(),
                product.getClasseCodice(),
                product.getSottoclasseCodice(),
                product.getIsAttrezzatura(),
                product.getIsAccessorio(),
                product.getOrigine(),
                product.getAggiornatoIl()
        );
    }
}
