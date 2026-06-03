package finance.backend.bvnos.service;

import finance.backend.bvnos.exception.ReconciliationProcessException;
import finance.backend.bvnos.model.*;
import finance.backend.bvnos.repository.ReconciliationProcessRepository;
import finance.backend.bvnos.repository.ReconciliationResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationProcessServiceImplTest {

    @Mock
    private ReconciliationProcessRepository reconciliationProcessRepository;

    @Mock
    private ReconciliationResultRepository reconciliationResultRepository;

    @Mock
    private ExcelParserService excelParserService;

    @Mock
    private ReconciliationEngineService reconciliationEngineService;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ReconciliationProcessServiceImpl reconciliationProcessService;

    @Captor
    private ArgumentCaptor<ReconciliationProcess> processCaptor;

    @Mock
    private MultipartFile fileA;

    @Mock
    private MultipartFile fileB;

    private ReconciliationProcessRequestDTO requestDTO;
    private ReconciliationProcess process;
    private List<PaymentRecord> sourceARecords;
    private List<PaymentRecord> sourceBRecords;
    private List<ReconciliationResult> engineResults;
    private ReconciliationResult reconciledResult;
    private ReconciliationResult mismatchResult;

    @BeforeEach
    void setUp() {
        requestDTO = new ReconciliationProcessRequestDTO();
        requestDTO.setName("Test Reconciliation");
        requestDTO.setFileA(fileA);
        requestDTO.setFileB(fileB);

        process = new ReconciliationProcess();
        process.setId("proc-1");
        process.setName("Test Reconciliation");
        process.setStatus(ReconciliationProcessStatus.PROCESSING);

        FileInformation fileInfoA = new FileInformation();
        fileInfoA.setFileName("vesta.xlsx");
        fileInfoA.setUploadedAt(LocalDateTime.now());

        FileInformation fileInfoB = new FileInformation();
        fileInfoB.setFileName("aax.xlsx");
        fileInfoB.setUploadedAt(LocalDateTime.now());

        process.setFileA(fileInfoA);
        process.setFileB(fileInfoB);
        process.setCreatedAt(LocalDateTime.now());

        sourceARecords = List.of(
                PaymentRecord.builder()
                        .customerId("CUST-001").service("SVC-A")
                        .amount(new BigDecimal("1000.00")).source("VESTA").build(),
                PaymentRecord.builder()
                        .customerId("CUST-002").service("SVC-B")
                        .amount(new BigDecimal("2000.00")).source("VESTA").build()
        );

        sourceBRecords = List.of(
                PaymentRecord.builder()
                        .customerId("CUST-001").service("SVC-A")
                        .amount(new BigDecimal("1000.00")).source("AAX").build(),
                PaymentRecord.builder()
                        .customerId("CUST-002").service("SVC-B")
                        .amount(new BigDecimal("1900.00")).source("AAX").build()
        );

        reconciledResult = new ReconciliationResult();
        reconciledResult.setId("res-1");
        reconciledResult.setProcessId("proc-1");
        reconciledResult.setCustomerId("CUST-001");
        reconciledResult.setService("SVC-A");
        reconciledResult.setStatus(ReconciliationStatus.RECONCILED);
        reconciledResult.setDifference(BigDecimal.ZERO);
        reconciledResult.setFinalAmount(new BigDecimal("1000.00"));
        reconciledResult.setActive(true);
        reconciledResult.setCreatedAt(LocalDateTime.now());
        reconciledResult.setReconciledAt(LocalDateTime.now());

        ReconciliationSource sourceA = new ReconciliationSource();
        sourceA.setSystem("VESTA");
        sourceA.setOriginalAmount(new BigDecimal("1000.00"));
        sourceA.setAdjustedAmount(new BigDecimal("1000.00"));

        ReconciliationSource sourceB = new ReconciliationSource();
        sourceB.setSystem("AAX");
        sourceB.setOriginalAmount(new BigDecimal("1000.00"));
        sourceB.setAdjustedAmount(new BigDecimal("1000.00"));

        reconciledResult.setSourceA(sourceA);
        reconciledResult.setSourceB(sourceB);

        mismatchResult = new ReconciliationResult();
        mismatchResult.setId("res-2");
        mismatchResult.setProcessId("proc-1");
        mismatchResult.setCustomerId("CUST-002");
        mismatchResult.setService("SVC-B");
        mismatchResult.setStatus(ReconciliationStatus.MISMATCH);
        mismatchResult.setDifference(new BigDecimal("100.00"));
        mismatchResult.setActive(true);
        mismatchResult.setCreatedAt(LocalDateTime.now());

        ReconciliationSource sourceA2 = new ReconciliationSource();
        sourceA2.setSystem("VESTA");
        sourceA2.setOriginalAmount(new BigDecimal("2000.00"));
        sourceA2.setAdjustedAmount(new BigDecimal("2000.00"));

        ReconciliationSource sourceB2 = new ReconciliationSource();
        sourceB2.setSystem("AAX");
        sourceB2.setOriginalAmount(new BigDecimal("1900.00"));
        sourceB2.setAdjustedAmount(new BigDecimal("1900.00"));

        mismatchResult.setSourceA(sourceA2);
        mismatchResult.setSourceB(sourceB2);

        Adjustment adjustment = new Adjustment();
        adjustment.setApplied(false);
        mismatchResult.setAdjustment(adjustment);

        engineResults = List.of(reconciledResult, mismatchResult);
    }

    @Nested
    @DisplayName("processReconciliation tests")
    class ProcessReconciliationTests {

        @Test
        @DisplayName("Should process reconciliation successfully")
        void shouldProcessReconciliationSuccessfully() throws IOException {
            when(fileA.getOriginalFilename()).thenReturn("vesta.xlsx");
            when(fileB.getOriginalFilename()).thenReturn("aax.xlsx");
            when(reconciliationProcessRepository.save(any(ReconciliationProcess.class)))
                    .thenAnswer(inv -> {
                        ReconciliationProcess p = inv.getArgument(0);
                        if (p.getId() == null) p.setId("proc-1");
                        return p;
                    });
            when(excelParserService.parseVesta(fileA)).thenReturn(sourceARecords);
            when(excelParserService.parseAax(fileB)).thenReturn(sourceBRecords);
            when(reconciliationEngineService.reconcile("proc-1", sourceARecords, sourceBRecords))
                    .thenReturn(engineResults);
            when(reconciliationResultRepository.saveAll(engineResults)).thenReturn(engineResults);

            ReconciliationProcessResponseDTO result = reconciliationProcessService.processReconciliation(requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("proc-1");
            assertThat(result.getName()).isEqualTo("Test Reconciliation");
            assertThat(result.getStatus()).isEqualTo("COMPLETED");
            assertThat(result.getTotalRecords()).isEqualTo(2);
            assertThat(result.getMatchedRecords()).isEqualTo(1);
            assertThat(result.getMismatchedRecords()).isEqualTo(1);
            assertThat(result.getFileA().getFileName()).isEqualTo("vesta.xlsx");
            assertThat(result.getFileB().getFileName()).isEqualTo("aax.xlsx");
            assertThat(result.getCreatedAt()).isNotNull();

            verify(reconciliationProcessRepository, times(2)).save(any(ReconciliationProcess.class));
            verify(excelParserService).parseVesta(fileA);
            verify(excelParserService).parseAax(fileB);
            verify(reconciliationEngineService).reconcile("proc-1", sourceARecords, sourceBRecords);
            verify(reconciliationResultRepository).saveAll(engineResults);
        }

        @Test
        @DisplayName("Should handle reconciliation failure and mark process as FAILED")
        void shouldHandleReconciliationFailure() throws IOException {
            when(fileA.getOriginalFilename()).thenReturn("vesta.xlsx");
            when(fileB.getOriginalFilename()).thenReturn("aax.xlsx");
            when(reconciliationProcessRepository.save(any(ReconciliationProcess.class)))
                    .thenAnswer(inv -> {
                        ReconciliationProcess p = inv.getArgument(0);
                        if (p.getId() == null) p.setId("proc-1");
                        return p;
                    });
            when(excelParserService.parseVesta(fileA))
                    .thenThrow(new RuntimeException("Invalid format in VESTA file"));

            assertThatThrownBy(() -> reconciliationProcessService.processReconciliation(requestDTO))
                    .isInstanceOf(ReconciliationProcessException.class)
                    .hasMessageContaining("The file 'vesta.xlsx' has an invalid format");

            verify(reconciliationProcessRepository, times(2)).save(processCaptor.capture());
            ReconciliationProcess savedProcess = processCaptor.getValue();
            assertThat(savedProcess.getStatus()).isEqualTo(ReconciliationProcessStatus.FAILED);
        }

        @Test
        @DisplayName("Should handle AAX file failure correctly")
        void shouldHandleAAXFileFailure() throws IOException {
            when(fileA.getOriginalFilename()).thenReturn("vesta.xlsx");
            when(fileB.getOriginalFilename()).thenReturn("aax.xlsx");
            when(reconciliationProcessRepository.save(any(ReconciliationProcess.class)))
                    .thenAnswer(inv -> {
                        ReconciliationProcess p = inv.getArgument(0);
                        if (p.getId() == null) p.setId("proc-1");
                        return p;
                    });
            when(excelParserService.parseVesta(fileA)).thenReturn(sourceARecords);
            when(excelParserService.parseAax(fileB))
                    .thenThrow(new RuntimeException("Invalid format in AAX file"));

            assertThatThrownBy(() -> reconciliationProcessService.processReconciliation(requestDTO))
                    .isInstanceOf(ReconciliationProcessException.class)
                    .hasMessageContaining("The file 'aax.xlsx' has an invalid format");

            verify(reconciliationProcessRepository, times(2)).save(any(ReconciliationProcess.class));
        }
    }

    @Nested
    @DisplayName("getResultsByProcess tests")
    class GetResultsByProcessTests {

        @Test
        @DisplayName("Should return results for a process")
        void shouldReturnResultsForProcess() {
            when(reconciliationResultRepository.findByProcessIdAndActiveTrue("proc-1"))
                    .thenReturn(List.of(reconciledResult, mismatchResult));

            List<ReconciliationResultResponseDTO> results =
                    reconciliationProcessService.getResultsByProcess("proc-1");

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getCustomerId()).isEqualTo("CUST-001");
            assertThat(results.get(0).getStatus()).isEqualTo("RECONCILED");
            assertThat(results.get(1).getCustomerId()).isEqualTo("CUST-002");
            assertThat(results.get(1).getStatus()).isEqualTo("MISMATCH");
        }

        @Test
        @DisplayName("Should return empty list when no results")
        void shouldReturnEmptyWhenNoResults() {
            when(reconciliationResultRepository.findByProcessIdAndActiveTrue("proc-1"))
                    .thenReturn(List.of());

            List<ReconciliationResultResponseDTO> results =
                    reconciliationProcessService.getResultsByProcess("proc-1");

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("applyAdjustment tests")
    class ApplyAdjustmentTests {

        @Test
        @DisplayName("Should apply adjustment and set status to RECONCILED when amounts match")
        void shouldApplyAdjustmentAndReconcile() {
            ApplyAdjustmentRequestDTO adjustmentDTO = new ApplyAdjustmentRequestDTO();
            adjustmentDTO.setVestaAdjustedAmount(new BigDecimal("950.00"));
            adjustmentDTO.setAaxAdjustedAmount(new BigDecimal("950.00"));
            adjustmentDTO.setAdjustedBy("John Doe");
            adjustmentDTO.setReason("Both parties agreed");

            when(reconciliationResultRepository.findById("res-2")).thenReturn(Optional.of(mismatchResult));
            when(reconciliationResultRepository.save(any(ReconciliationResult.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ReconciliationResultResponseDTO result =
                    reconciliationProcessService.applyAdjustment("res-2", adjustmentDTO);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("RECONCILED");
            assertThat(result.getFinalAmount()).isEqualByComparingTo(new BigDecimal("950.00"));
            assertThat(result.getDifference()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getReconciledAt()).isNotNull();

            assertThat(mismatchResult.getSourceA().getAdjustedAmount()).isEqualByComparingTo(new BigDecimal("950.00"));
            assertThat(mismatchResult.getSourceB().getAdjustedAmount()).isEqualByComparingTo(new BigDecimal("950.00"));
            assertThat(mismatchResult.getAdjustment().getApplied()).isTrue();
            assertThat(mismatchResult.getAdjustment().getAdjustedBy()).isEqualTo("John Doe");
            assertThat(mismatchResult.getAdjustment().getReason()).isEqualTo("Both parties agreed");
        }

        @Test
        @DisplayName("Should apply adjustment and keep MISMATCH when amounts differ")
        void shouldApplyAdjustmentAndKeepMismatch() {
            ApplyAdjustmentRequestDTO adjustmentDTO = new ApplyAdjustmentRequestDTO();
            adjustmentDTO.setVestaAdjustedAmount(new BigDecimal("1000.00"));
            adjustmentDTO.setAaxAdjustedAmount(new BigDecimal("900.00"));
            adjustmentDTO.setAdjustedBy("Jane Doe");
            adjustmentDTO.setReason("Partial adjustment");

            when(reconciliationResultRepository.findById("res-2")).thenReturn(Optional.of(mismatchResult));
            when(reconciliationResultRepository.save(any(ReconciliationResult.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ReconciliationResultResponseDTO result =
                    reconciliationProcessService.applyAdjustment("res-2", adjustmentDTO);

            assertThat(result.getStatus()).isEqualTo("MISMATCH");
            assertThat(result.getDifference()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getFinalAmount()).isNull();
            assertThat(result.getReconciledAt()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when reconciliation result not found")
        void shouldThrowWhenResultNotFound() {
            ApplyAdjustmentRequestDTO adjustmentDTO = new ApplyAdjustmentRequestDTO();
            adjustmentDTO.setVestaAdjustedAmount(BigDecimal.ZERO);
            adjustmentDTO.setAaxAdjustedAmount(BigDecimal.ZERO);

            when(reconciliationResultRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reconciliationProcessService.applyAdjustment("invalid-id", adjustmentDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Reconciliation result not found");
        }
    }

    @Nested
    @DisplayName("updateResult tests")
    class UpdateResultTests {

        @Test
        @DisplayName("Should update reconciled result amount")
        void shouldUpdateReconciledResult() {
            UpdateReconciliationResultRequestDTO updateDTO = new UpdateReconciliationResultRequestDTO();
            updateDTO.setFinalAmount(new BigDecimal("1100.00"));

            when(reconciliationResultRepository.findById("res-1")).thenReturn(Optional.of(reconciledResult));
            when(reconciliationResultRepository.save(any(ReconciliationResult.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ReconciliationResultResponseDTO result =
                    reconciliationProcessService.updateResult("res-1", updateDTO);

            assertThat(result.getFinalAmount()).isEqualByComparingTo(new BigDecimal("1100.00"));
        }

        @Test
        @DisplayName("Should throw when result not found")
        void shouldThrowWhenNotFound() {
            when(reconciliationResultRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    reconciliationProcessService.updateResult("invalid-id", new UpdateReconciliationResultRequestDTO()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Result not found");
        }

        @Test
        @DisplayName("Should throw when result is not active")
        void shouldThrowWhenNotActive() {
            reconciledResult.setActive(false);
            when(reconciliationResultRepository.findById("res-1")).thenReturn(Optional.of(reconciledResult));

            assertThatThrownBy(() ->
                    reconciliationProcessService.updateResult("res-1", new UpdateReconciliationResultRequestDTO()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cannot edit deleted result");
        }

        @Test
        @DisplayName("Should throw when result is not RECONCILED")
        void shouldThrowWhenNotReconciled() {
            when(reconciliationResultRepository.findById("res-2")).thenReturn(Optional.of(mismatchResult));

            assertThatThrownBy(() ->
                    reconciliationProcessService.updateResult("res-2", new UpdateReconciliationResultRequestDTO()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Only reconciled records can be edited");
        }
    }

    @Nested
    @DisplayName("deleteResult tests")
    class DeleteResultTests {

        @Test
        @DisplayName("Should soft delete reconciliation result")
        void shouldSoftDeleteResult() {
            when(reconciliationResultRepository.findById("res-1")).thenReturn(Optional.of(reconciledResult));
            when(reconciliationResultRepository.save(any(ReconciliationResult.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            reconciliationProcessService.deleteResult("res-1");

            assertThat(reconciledResult.getActive()).isFalse();
            assertThat(reconciledResult.getDeletedAt()).isNotNull();
            verify(reconciliationResultRepository).save(reconciledResult);
        }

        @Test
        @DisplayName("Should throw when result not found")
        void shouldThrowWhenNotFound() {
            when(reconciliationResultRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reconciliationProcessService.deleteResult("invalid-id"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Result not found");
        }
    }

    @Nested
    @DisplayName("getResultsByProcessPaginated tests")
    class GetResultsByProcessPaginatedTests {

        @Test
        @DisplayName("Should return paginated results without filters")
        void shouldReturnPaginatedResults() {
            Query captorQuery = new Query();
            when(mongoTemplate.find(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(List.of(reconciledResult, mismatchResult));
            when(mongoTemplate.count(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(2L);

            Map<String, Object> result = reconciliationProcessService.getResultsByProcessPaginated(
                    "proc-1", 0, 10, null, null, null, null
            );

            assertThat(result).containsKeys("content", "currentPage", "totalItems", "totalPages");
            assertThat(result.get("currentPage")).isEqualTo(0);
            assertThat(result.get("totalItems")).isEqualTo(2L);
            assertThat(result.get("totalPages")).isEqualTo(1);

            List<ReconciliationResultResponseDTO> content = (List<ReconciliationResultResponseDTO>) result.get("content");
            assertThat(content).hasSize(2);
        }

        @Test
        @DisplayName("Should apply filters to paginated query")
        void shouldApplyFilters() {
            when(mongoTemplate.find(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(List.of(reconciledResult));
            when(mongoTemplate.count(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(1L);

            Map<String, Object> result = reconciliationProcessService.getResultsByProcessPaginated(
                    "proc-1", 0, 10, "CUST-001", "SVC-A", "RECONCILED", "2025-01-15"
            );

            assertThat(result.get("totalItems")).isEqualTo(1L);

            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            verify(mongoTemplate).find(queryCaptor.capture(), eq(ReconciliationResult.class));
            Query capturedQuery = queryCaptor.getValue();

            assertThat(capturedQuery.getQueryObject().toString()).contains("proc-1");
            assertThat(capturedQuery.getQueryObject().toString()).contains("RECONCILED");
        }

        @Test
        @DisplayName("Should ignore invalid date format")
        void shouldIgnoreInvalidDateFormat() {
            when(mongoTemplate.find(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(List.of());
            when(mongoTemplate.count(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(0L);

            Map<String, Object> result = reconciliationProcessService.getResultsByProcessPaginated(
                    "proc-1", 0, 10, null, null, null, "invalid-date"
            );

            assertThat(result.get("totalItems")).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("getMonthlyResultsPaginated tests")
    class GetMonthlyResultsPaginatedTests {

        @Test
        @DisplayName("Should return paginated monthly results")
        void shouldReturnMonthlyResults() {
            when(mongoTemplate.find(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(List.of(reconciledResult));
            when(mongoTemplate.count(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(1L);

            Map<String, Object> result = reconciliationProcessService.getMonthlyResultsPaginated(
                    2025, 1, 0, 10, null, null, null, null, null
            );

            assertThat(result).containsKeys("content", "currentPage", "totalItems", "totalPages");
            assertThat(result.get("totalItems")).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should include inactive records when requested")
        void shouldIncludeInactiveRecords() {
            when(mongoTemplate.find(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(List.of(reconciledResult));
            when(mongoTemplate.count(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(1L);

            Map<String, Object> result = reconciliationProcessService.getMonthlyResultsPaginated(
                    2025, 1, 0, 10, null, null, null, null, "false"
            );

            assertThat(result.get("totalItems")).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should apply date filter instead of month range when date is provided")
        void shouldApplyDateFilter() {
            when(mongoTemplate.find(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(List.of(reconciledResult));
            when(mongoTemplate.count(any(Query.class), eq(ReconciliationResult.class)))
                    .thenReturn(1L);

            Map<String, Object> result = reconciliationProcessService.getMonthlyResultsPaginated(
                    2025, 1, 0, 10, null, null, null, "2025-01-15", "true"
            );

            assertThat(result.get("totalItems")).isEqualTo(1L);

            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            verify(mongoTemplate).find(queryCaptor.capture(), eq(ReconciliationResult.class));
            Query capturedQuery = queryCaptor.getValue();
            assertThat(capturedQuery.getQueryObject().toString()).contains("2025-01-15");
        }
    }
}
