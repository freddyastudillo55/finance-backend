package finance.backend.bvnos.service;

import finance.backend.bvnos.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReconciliationEngineServiceImpl implements ReconciliationEngineService {

    @Override
    public List<ReconciliationResult> reconcile(String processId, List<PaymentRecord> sourceA, List<PaymentRecord> sourceB) {

        List<ReconciliationResult> results = new ArrayList<>();

        Map<String, PaymentRecord> sourceAMap = sourceA.stream()
                .collect(Collectors.toMap(
                        this::buildKey,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        for (PaymentRecord recordB : sourceB) {

            String key = buildKey(recordB);

            PaymentRecord recordA = sourceAMap.get(key);

            if (recordA == null) {
                continue;
            }

            ReconciliationResult result = buildResult(
                    processId,
                    recordA,
                    recordB
            );

            results.add(result);
        }

        return results;
    }

    private String buildKey(PaymentRecord record) {

        return record.getCustomerId()
                + "_"
                + record.getService();
    }

    private ReconciliationResult buildResult(
            String processId,
            PaymentRecord recordA,
            PaymentRecord recordB
    ) {

        ReconciliationResult result = new ReconciliationResult();

        result.setProcessId(processId);
        result.setCustomerId(recordA.getCustomerId());
        result.setService(recordA.getService());

        result.setSourceA(buildSource(recordA));
        result.setSourceB(buildSource(recordB));

        result.setCreatedAt(LocalDateTime.now());
        result.setActive(true);

        BigDecimal difference = recordA.getAmount()
                .subtract(recordB.getAmount())
                .abs();

        result.setDifference(difference);

        if (difference.compareTo(BigDecimal.ZERO) == 0) {

            result.setStatus(ReconciliationStatus.RECONCILED);
            result.setFinalAmount(recordA.getAmount());
            result.setReconciledAt(LocalDateTime.now());

        } else {

            result.setStatus(ReconciliationStatus.MISMATCH);
            result.setFinalAmount(null);

            Adjustment adjustment = new Adjustment();

            adjustment.setApplied(false);

            result.setAdjustment(adjustment);
        }

        return result;
    }

    private ReconciliationSource buildSource(PaymentRecord record) {

        ReconciliationSource source = new ReconciliationSource();

        source.setSystem(record.getSource());
        source.setOriginalAmount(record.getAmount());
        source.setAdjustedAmount(record.getAmount());

        return source;

    }

}
