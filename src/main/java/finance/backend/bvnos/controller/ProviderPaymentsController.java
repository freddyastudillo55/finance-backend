package finance.backend.bvnos.controller;

import finance.backend.bvnos.model.ProviderPaymentRequestDTO;
import finance.backend.bvnos.model.ProviderPaymentResponseDTO;
import finance.backend.bvnos.service.ProviderPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/provider-payments")
public class ProviderPaymentsController {

    private final ProviderPaymentService providerPaymentService;

    @PostMapping("/create")
    public ProviderPaymentResponseDTO savePayment(
            @RequestBody ProviderPaymentRequestDTO requestDTO
    ) {

        return providerPaymentService.savePayment(requestDTO);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProviderPaymentResponseDTO> updatePayment(@PathVariable String id, @RequestBody ProviderPaymentRequestDTO requestDTO) {
        ProviderPaymentResponseDTO updated = providerPaymentService.updatePayment(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getPayments")
    public List<ProviderPaymentResponseDTO> getAllPayments() {

        return providerPaymentService.getAllPayments();
    }

    @GetMapping("/getActivePayments")
    public List<ProviderPaymentResponseDTO> getAllActivePayments() {

        return providerPaymentService.getAllActivePayments();
    }

    @GetMapping("/getPaymentsByProvider/{providerId}")
    public List<ProviderPaymentResponseDTO> getPaymentsByProvider(
            @PathVariable String providerId
    ) {

        return providerPaymentService.getPaymentsByProvider(providerId);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {

        boolean deleted = providerPaymentService.deletePayment(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

}
