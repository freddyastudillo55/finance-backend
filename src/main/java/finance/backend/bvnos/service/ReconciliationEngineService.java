package finance.backend.bvnos.service;

import finance.backend.bvnos.model.PaymentRecord;
import finance.backend.bvnos.model.ReconciliationResult;

import java.util.List;

public interface ReconciliationEngineService {
    List<ReconciliationResult> reconcile(String processId, List<PaymentRecord> sourceA, List<PaymentRecord> sourceB);
}
