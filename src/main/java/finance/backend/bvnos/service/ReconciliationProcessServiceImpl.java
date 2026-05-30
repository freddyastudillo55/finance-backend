package finance.backend.bvnos.service;

import finance.backend.bvnos.exception.ReconciliationProcessException;
import finance.backend.bvnos.model.*;
import finance.backend.bvnos.repository.ReconciliationProcessRepository;
import finance.backend.bvnos.repository.ReconciliationResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationProcessServiceImpl implements ReconciliationProcessService {

    private final ReconciliationProcessRepository reconciliationProcessRepository;

    private final ReconciliationResultRepository reconciliationResultRepository;

    private final ExcelParserService excelParserService;

    private final ReconciliationEngineService reconciliationEngineService;

    private final MongoTemplate mongoTemplate;

    @Override
    public ReconciliationProcessResponseDTO processReconciliation(ReconciliationProcessRequestDTO requestDTO) throws IOException {
        ReconciliationProcess process = new ReconciliationProcess();
        process.setName(requestDTO.getName());
        process.setStatus(ReconciliationProcessStatus.PROCESSING);
        process.setCreatedAt(LocalDateTime.now());

        FileInformation fileA = new FileInformation();
        fileA.setFileName(requestDTO.getFileA().getOriginalFilename());
        fileA.setUploadedAt(LocalDateTime.now());

        FileInformation fileB = new FileInformation();
        fileB.setFileName(requestDTO.getFileB().getOriginalFilename());
        fileB.setUploadedAt(LocalDateTime.now());

        process.setFileA(fileA);
        process.setFileB(fileB);

        process = reconciliationProcessRepository.save(process);

        try {
            List<PaymentRecord> sourceA = excelParserService.parseVesta(requestDTO.getFileA());
            List<PaymentRecord> sourceB = excelParserService.parseAax(requestDTO.getFileB());

            List<ReconciliationResult> results = reconciliationEngineService.reconcile(
                    process.getId(), sourceA, sourceB
            );

            reconciliationResultRepository.saveAll(results);

            long matchedRecords = results.stream()
                    .filter(r -> r.getStatus() == ReconciliationStatus.RECONCILED)
                    .count();
            long mismatchedRecords = results.stream()
                    .filter(r -> r.getStatus() == ReconciliationStatus.MISMATCH)
                    .count();

            process.setTotalRecords(results.size());
            process.setMatchedRecords((int) matchedRecords);
            process.setMismatchedRecords((int) mismatchedRecords);
            process.setStatus(ReconciliationProcessStatus.COMPLETED);

            reconciliationProcessRepository.save(process);

            return mapProcess(process);

        } catch (Exception e) {
            log.error("Reconciliation process failed for process {}: {}", process.getId(), e.getMessage(), e);

            process.setStatus(ReconciliationProcessStatus.FAILED);
            reconciliationProcessRepository.save(process);

            String fileName = e.getMessage() != null && e.getMessage().contains("VESTA")
                    ? fileA.getFileName() : fileB.getFileName();

            throw new ReconciliationProcessException(
                    "It was not possible to perform the reconciliation. " +
                            "The file '" + fileName + "' has an invalid format or contains unexpected data: " + e.getMessage()
            );
        }
    }

    @Override
    public List<ReconciliationResultResponseDTO> getResultsByProcess(String processId) {

        List<ReconciliationResult> results = reconciliationResultRepository.findByProcessIdAndActiveTrue(processId);

        return results.stream()
                .map(this::mapResult)
                .toList();
    }

    private ReconciliationProcessResponseDTO mapProcess(ReconciliationProcess process) {

        ReconciliationProcessResponseDTO dto =
                new ReconciliationProcessResponseDTO();

        dto.setId(process.getId());
        dto.setName(process.getName());
        dto.setFileA(process.getFileA());
        dto.setFileB(process.getFileB());
        dto.setStatus(process.getStatus().name());
        dto.setTotalRecords(process.getTotalRecords());
        dto.setMatchedRecords(process.getMatchedRecords());
        dto.setMismatchedRecords(process.getMismatchedRecords());
        dto.setCreatedAt(process.getCreatedAt());

        return dto;
    }

    private ReconciliationResultResponseDTO mapResult(ReconciliationResult result) {

        ReconciliationResultResponseDTO dto = new ReconciliationResultResponseDTO();

        dto.setId(result.getId());
        dto.setProcessId(result.getProcessId());
        dto.setCustomerId(result.getCustomerId());
        dto.setService(result.getService());
        dto.setSourceA(result.getSourceA());
        dto.setSourceB(result.getSourceB());
        dto.setFinalAmount(result.getFinalAmount());
        dto.setStatus(result.getStatus().name());
        dto.setDifference(result.getDifference());
        dto.setAdjustment(result.getAdjustment());
        dto.setCreatedAt(result.getCreatedAt());
        dto.setReconciledAt(result.getReconciledAt());
        dto.setActive(result.getActive());

        return dto;
    }

    @Override
    public ReconciliationResultResponseDTO applyAdjustment(
            String reconciliationResultId,
            ApplyAdjustmentRequestDTO requestDTO
    ) {

        ReconciliationResult result =
                reconciliationResultRepository
                        .findById(reconciliationResultId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reconciliation result not found"
                                )
                        );

        BigDecimal vestaAmount =
                requestDTO.getVestaAdjustedAmount();

        BigDecimal aaxAmount =
                requestDTO.getAaxAdjustedAmount();

        result.getSourceA()
                .setAdjustedAmount(vestaAmount);

        result.getSourceB()
                .setAdjustedAmount(aaxAmount);

        Adjustment adjustment = new Adjustment();

        adjustment.setApplied(true);

        adjustment.setAdjustedBy(
                requestDTO.getAdjustedBy()
        );

        adjustment.setAdjustedAt(LocalDateTime.now());

        adjustment.setReason(
                requestDTO.getReason()
        );

        result.setAdjustment(adjustment);

        if (vestaAmount.compareTo(aaxAmount) == 0) {

            result.setStatus(
                    ReconciliationStatus.RECONCILED
            );

            result.setFinalAmount(vestaAmount);

            result.setDifference(BigDecimal.ZERO);

            result.setReconciledAt(LocalDateTime.now());

        } else {

            BigDecimal difference = vestaAmount
                    .subtract(aaxAmount)
                    .abs();

            result.setStatus(
                    ReconciliationStatus.MISMATCH
            );

            result.setDifference(difference);

            result.setFinalAmount(null);

            result.setReconciledAt(null);
        }

        reconciliationResultRepository.save(result);

        return mapResult(result);
    }

    @Override
    public ReconciliationResultResponseDTO updateResult(String id, UpdateReconciliationResultRequestDTO requestDTO) {

        ReconciliationResult result =
                reconciliationResultRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("Result not found"));

        if (!result.getActive()) {
            throw new RuntimeException(
                    "Cannot edit deleted result"
            );
        }

        if (result.getStatus()
                != ReconciliationStatus.RECONCILED) {

            throw new RuntimeException(
                    "Only reconciled records can be edited"
            );
        }

        result.setFinalAmount(
                requestDTO.getFinalAmount()
        );

        reconciliationResultRepository.save(result);

        return mapResult(result);
    }

    @Override
    public void deleteResult(String id) {

        ReconciliationResult result =
                reconciliationResultRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Result not found"
                                )
                        );

        result.setActive(false);

        result.setDeletedAt(
                LocalDateTime.now()
        );

        reconciliationResultRepository.save(result);
    }

    @Override
    public Map<String, Object> getResultsByProcessPaginated(
            String processId, int page, int size,
            String customerId, String service, String status, String date) {

        Pageable pageable = PageRequest.of(page, size);
        Query query = new Query().with(pageable);

        query.addCriteria(Criteria.where("processId").is(processId).and("active").is(true));

        if (date != null && !date.isBlank()) {
            try {
                LocalDate localDate = LocalDate.parse(date);
                LocalDateTime startOfDay = localDate.atStartOfDay();
                LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
                query.addCriteria(Criteria.where("createdAt").gte(startOfDay).lte(endOfDay));
            } catch (DateTimeParseException e) {
                log.warn("Invalid date format for filter: {}", date);
            }
        }

        if (customerId != null && !customerId.isBlank()) {
            query.addCriteria(Criteria.where("customerId").regex(customerId, "i"));
        }

        if (service != null && !service.isBlank()) {
            query.addCriteria(Criteria.where("service").regex(service, "i"));
        }

        if (status != null && !status.isBlank()) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        List<ReconciliationResult> list = mongoTemplate.find(query, ReconciliationResult.class);

        Query countQuery = Query.of(query).limit(-1).skip(-1);
        long total = mongoTemplate.count(countQuery, ReconciliationResult.class);

        Page<ReconciliationResult> pageResult = PageableExecutionUtils.getPage(list, pageable, () -> total);

        List<ReconciliationResultResponseDTO> dtos = pageResult.getContent().stream()
                .map(this::mapResult)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtos);
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());

        return response;
    }

    @Override
    public Map<String, Object> getMonthlyResultsPaginated(int year, int month, int page, int size, String customerId, String service, String status, String date, String active) {
        Pageable pageable = PageRequest.of(page, size);
        Query query = new Query().with(pageable);

        if (active != null && !active.isBlank()) {
            boolean isActive = Boolean.parseBoolean(active);
            query.addCriteria(Criteria.where("active").is(isActive));
        } else {
            query.addCriteria(Criteria.where("active").is(true));
        }

        if (date != null && !date.isBlank()) {
            try {
                LocalDate localDate = LocalDate.parse(date);
                LocalDateTime startOfDay = localDate.atStartOfDay();
                LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
                query.addCriteria(Criteria.where("createdAt").gte(startOfDay).lte(endOfDay));
            } catch (DateTimeParseException e) {
                log.warn("Invalid date format for filter: {}", date);
            }
        } else {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
            query.addCriteria(Criteria.where("createdAt").gte(startOfMonth).lte(endOfMonth));
        }

        if (customerId != null && !customerId.isBlank()) {
            query.addCriteria(Criteria.where("customerId").regex(customerId, "i"));
        }

        if (service != null && !service.isBlank()) {
            query.addCriteria(Criteria.where("service").regex(service, "i"));
        }

        if (status != null && !status.isBlank()) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        List<ReconciliationResult> list = mongoTemplate.find(query, ReconciliationResult.class);

        Query countQuery = Query.of(query).limit(-1).skip(-1);
        long total = mongoTemplate.count(countQuery, ReconciliationResult.class);

        Page<ReconciliationResult> pageResult = PageableExecutionUtils.getPage(list, pageable, () -> total);

        List<ReconciliationResultResponseDTO> dtos = pageResult.getContent().stream()
                .map(this::mapResult)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtos);
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());

        return response;
    }

}
