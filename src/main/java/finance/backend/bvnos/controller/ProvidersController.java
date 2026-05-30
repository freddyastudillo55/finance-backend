package finance.backend.bvnos.controller;

import finance.backend.bvnos.model.ProviderDashboardResponseDTO;
import finance.backend.bvnos.model.ProviderRequestDTO;
import finance.backend.bvnos.model.ProviderResponseDTO;
import finance.backend.bvnos.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/providers")
public class ProvidersController {

    private final ProviderService providerService;

    @PostMapping("/create")
    private ProviderResponseDTO saveProvider (@RequestBody ProviderRequestDTO requestDTO){
        return providerService.saveProvider(requestDTO);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProviderResponseDTO> updateProvider(@PathVariable String id, @RequestBody ProviderRequestDTO requestDTO) {
        ProviderResponseDTO updated = providerService.updateProvider(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getProviders")
    public List<ProviderDashboardResponseDTO> getAll() {
        return providerService.getProvidersForDashboard();
    }

    @GetMapping("/getActiveProviders")
    public List<ProviderDashboardResponseDTO> getAllActive() {
        return providerService.getActiveProvidersForDashboard();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {

        boolean deleted = providerService.deleteProvider(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

}
