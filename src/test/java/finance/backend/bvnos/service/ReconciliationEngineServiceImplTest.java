package finance.backend.bvnos.service;

import finance.backend.bvnos.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationEngineServiceImplTest {

    private ReconciliationEngineServiceImpl engineService;

    private PaymentRecord recordA1;
    private PaymentRecord recordA2;
    private PaymentRecord recordB1;
    private PaymentRecord recordB2;
    private PaymentRecord recordB3;

    @BeforeEach
    void setUp() {
        engineService = new ReconciliationEngineServiceImpl();

        recordA1 = PaymentRecord.builder()
                .customerId("CUST-001")
                .service("SERVICE-A")
                .amount(new BigDecimal("1000.00"))
                .paymentDate(LocalDate.of(2025, 1, 15))
                .source("VESTA")
                .build();

        recordA2 = PaymentRecord.builder()
                .customerId("CUST-002")
                .service("SERVICE-B")
                .amount(new BigDecimal("2000.00"))
                .paymentDate(LocalDate.of(2025, 1, 20))
                .source("VESTA")
                .build();

        // Same key as recordA1 (CUST-001_SERVICE-A) but different amount -> MISMATCH
        recordB1 = PaymentRecord.builder()
                .customerId("CUST-001")
                .service("SERVICE-A")
                .amount(new BigDecimal("950.00"))
                .paymentDate(LocalDate.of(2025, 1, 15))
                .source("AAX")
                .build();

        // Same key as recordA2 and same amount -> RECONCILED
        recordB2 = PaymentRecord.builder()
                .customerId("CUST-002")
                .service("SERVICE-B")
                .amount(new BigDecimal("2000.00"))
                .paymentDate(LocalDate.of(2025, 1, 20))
                .source("AAX")
                .build();

        // No matching record in sourceA -> should be skipped
        recordB3 = PaymentRecord.builder()
                .customerId("CUST-003")
                .service("SERVICE-C")
                .amount(new BigDecimal("3000.00"))
                .paymentDate(LocalDate.of(2025, 1, 25))
                .source("AAX")
                .build();
    }

    @Test
    @DisplayName("Should reconcile matching records correctly")
    void shouldReconcileMatchingRecords() {
        List<ReconciliationResult> results = engineService.reconcile(
                "process-1",
                List.of(recordA1, recordA2),
                List.of(recordB1, recordB2, recordB3)
        );

        assertThat(results).hasSize(2);

        // First result: MISMATCH (1000 vs 950)
        ReconciliationResult result1 = results.stream()
                .filter(r -> r.getCustomerId().equals("CUST-001"))
                .findFirst()
                .orElseThrow();

        assertThat(result1.getProcessId()).isEqualTo("process-1");
        assertThat(result1.getCustomerId()).isEqualTo("CUST-001");
        assertThat(result1.getService()).isEqualTo("SERVICE-A");
        assertThat(result1.getStatus()).isEqualTo(ReconciliationStatus.MISMATCH);
        assertThat(result1.getDifference()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result1.getFinalAmount()).isNull();
        assertThat(result1.getAdjustment()).isNotNull();
        assertThat(result1.getAdjustment().getApplied()).isFalse();
        assertThat(result1.getActive()).isTrue();
        assertThat(result1.getSourceA().getSystem()).isEqualTo("VESTA");
        assertThat(result1.getSourceB().getSystem()).isEqualTo("AAX");
        assertThat(result1.getSourceA().getOriginalAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result1.getSourceB().getOriginalAmount()).isEqualByComparingTo(new BigDecimal("950.00"));
        assertThat(result1.getSourceA().getAdjustedAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result1.getSourceB().getAdjustedAmount()).isEqualByComparingTo(new BigDecimal("950.00"));

        // Second result: RECONCILED (2000 vs 2000)
        ReconciliationResult result2 = results.stream()
                .filter(r -> r.getCustomerId().equals("CUST-002"))
                .findFirst()
                .orElseThrow();

        assertThat(result2.getProcessId()).isEqualTo("process-1");
        assertThat(result2.getCustomerId()).isEqualTo("CUST-002");
        assertThat(result2.getService()).isEqualTo("SERVICE-B");
        assertThat(result2.getStatus()).isEqualTo(ReconciliationStatus.RECONCILED);
        assertThat(result2.getDifference()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result2.getFinalAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(result2.getReconciledAt()).isNotNull();
        assertThat(result2.getAdjustment()).isNull();
    }

    @Test
    @DisplayName("Should handle duplicate keys in source A by taking first occurrence")
    void shouldHandleDuplicateKeysInSourceA() {
        PaymentRecord duplicateA1 = PaymentRecord.builder()
                .customerId("CUST-001")
                .service("SERVICE-A")
                .amount(new BigDecimal("1000.00"))
                .source("VESTA")
                .build();

        PaymentRecord duplicateA2 = PaymentRecord.builder()
                .customerId("CUST-001")
                .service("SERVICE-A")
                .amount(new BigDecimal("999.00"))
                .source("VESTA")
                .build();

        List<ReconciliationResult> results = engineService.reconcile(
                "process-1",
                List.of(duplicateA1, duplicateA2),
                List.of(recordB1)
        );

        // Only one result should be produced for CUST-001_SERVICE-A
        assertThat(results).hasSize(1);
        // The first occurrence (1000.00) should be used, so difference = |1000 - 950| = 50
        assertThat(results.get(0).getDifference()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should return empty list when no records match between sources")
    void shouldReturnEmptyWhenNoMatches() {
        List<PaymentRecord> sourceA = List.of(recordA1, recordA2);
        List<PaymentRecord> sourceB = List.of(recordB3);

        List<ReconciliationResult> results = engineService.reconcile("process-1", sourceA, sourceB);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when both sources are empty")
    void shouldReturnEmptyWhenBothSourcesEmpty() {
        List<ReconciliationResult> results = engineService.reconcile(
                "process-1",
                List.of(),
                List.of()
        );

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when source B is empty")
    void shouldReturnEmptyWhenSourceBEmpty() {
        List<ReconciliationResult> results = engineService.reconcile(
                "process-1",
                List.of(recordA1, recordA2),
                List.of()
        );

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should set correct source system names")
    void shouldSetCorrectSourceSystems() {
        List<ReconciliationResult> results = engineService.reconcile(
                "process-1",
                List.of(recordA1),
                List.of(recordB1)
        );

        assertThat(results).hasSize(1);
        ReconciliationResult result = results.get(0);
        assertThat(result.getSourceA().getSystem()).isEqualTo("VESTA");
        assertThat(result.getSourceB().getSystem()).isEqualTo("AAX");
    }

    @Test
    @DisplayName("Should set createdAt for reconciliation results")
    void shouldSetCreatedAt() {
        List<ReconciliationResult> results = engineService.reconcile(
                "process-1",
                List.of(recordA1),
                List.of(recordB1)
        );

        assertThat(results.get(0).getCreatedAt()).isNotNull();
    }
}
