package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.request.MagazzinoRequest;
import pilot.servizi.dto.response.MagazzinoResponse;
import pilot.servizi.service.MagazzinoDigitaleService;

import java.util.List;

@RestController
@RequestMapping("/api/magazzino")
@Tag(name = "Magazzino", description = "Gestione ubicazioni di magazzino")
@RequiredArgsConstructor
public class MagazzinoController {

    private final MagazzinoDigitaleService service;

    @PostMapping
    @Operation(summary = "Crea ubicazione")
    @ResponseStatus(HttpStatus.CREATED)
    public MagazzinoResponse create(@Valid @RequestBody MagazzinoRequest dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public MagazzinoResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<MagazzinoResponse> getAll() {
        return service.getAll();
    }

    @PutMapping("/{id}")
    public MagazzinoResponse update(@PathVariable Long id, @Valid @RequestBody MagazzinoRequest dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}