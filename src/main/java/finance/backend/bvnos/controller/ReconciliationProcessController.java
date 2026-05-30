package finance.backend.bvnos.controller;

import finance.backend.bvnos.exception.ReconciliationProcessException;
import finance.backend.bvnos.model.*;
import finance.backend.bvnos.service.ReconciliationProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reconciliation")
public class ReconciliationProcessController {

    private final ReconciliationProcessService reconciliationProcessService;

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReconciliationProcessResponseDTO processReconciliation(@ModelAttribute ReconciliationProcessRequestDTO requestDTO) throws IOException {
        return reconciliationProcessService.processReconciliation(requestDTO);
    }

    @GetMapping("/results/{processId}")
    public ResponseEntity<Map<String, Object>> getResultsByProcess(
            @PathVariable String processId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date
    ) {
        return ResponseEntity.ok(reconciliationProcessService.getResultsByProcessPaginated(
                processId, page, size, customerId, service, status, date
        ));
    }

    @PatchMapping("/results/{id}/adjust")
    public ReconciliationResultResponseDTO applyAdjustment(@PathVariable String id, @RequestBody ApplyAdjustmentRequestDTO requestDTO) {
        return reconciliationProcessService.applyAdjustment(id, requestDTO);
    }

    @PutMapping("/results/{id}")
    public ReconciliationResultResponseDTO updateResult(@PathVariable String id, @RequestBody UpdateReconciliationResultRequestDTO requestDTO) {
        return reconciliationProcessService.updateResult(id, requestDTO);
    }

    @DeleteMapping("/results/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable String id) {
        reconciliationProcessService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/results/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyResults(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String active
    ) {
        return ResponseEntity.ok(reconciliationProcessService.getMonthlyResultsPaginated(
                year, month, page, size, customerId, service, status, date, active
        ));
    }

    @ExceptionHandler(ReconciliationProcessException.class)
    public ResponseEntity<Map<String, String>> handleReconciliationProcessException(ReconciliationProcessException e) {
        Map<String, String> body = new HashMap<>();
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "An unexpected error occurred while processing the files. Please verify the file format and try again.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
