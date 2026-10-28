package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.service.ErpSyncProxyService;

@RestController
@RequestMapping("/api/sync/erp")
@Tag(name = "Sync ERP", description = "Avvio della sincronizzazione ERP tramite credenziali server-to-server")
@RequiredArgsConstructor
public class ErpSyncController {

    private final ErpSyncProxyService service;

    @PostMapping
    @Operation(summary = "Avvia la sincronizzazione ERP; il token resta lato server")
    public ResponseEntity<byte[]> synchronize() {
        return service.synchronize();
    }
}
