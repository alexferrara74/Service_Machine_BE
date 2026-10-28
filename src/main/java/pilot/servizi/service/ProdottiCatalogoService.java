package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public List<ProdottoCatalogoResponse> getAll(Integer categoriaId, Boolean attrezzatura) {
        List<ProdottiCatalogo> products;
        if (categoriaId != null && attrezzatura != null) {
            products = repository.findByCategoriaIdAndIsAttrezzaturaOrderByCodiceAsc(categoriaId, attrezzatura);
        } else if (categoriaId != null) {
            products = repository.findByCategoriaIdOrderByCodiceAsc(categoriaId);
        } else if (Boolean.TRUE.equals(attrezzatura)) {
            products = repository.findByIsAttrezzaturaTrueOrderByCodiceAsc();
        } else if (Boolean.FALSE.equals(attrezzatura)) {
            products = repository.findByIsAttrezzaturaFalseOrderByCodiceAsc();
        } else {
            products = repository.findAllByOrderByCodiceAsc();
        }
        return products.stream().map(this::toResponse).toList();
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
                product.getAggiornatoIl()
        );
    }
}
