package finance.backend.bvnos.service;

import finance.backend.bvnos.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ReconciliationProcessService {

    ReconciliationProcessResponseDTO processReconciliation(ReconciliationProcessRequestDTO requestDTO) throws IOException;

    List<ReconciliationResultResponseDTO> getResultsByProcess(String processId);

    Map<String, Object> getResultsByProcessPaginated(String processId, int page, int size, String customerId, String service, String status, String date);

    Map<String, Object> getMonthlyResultsPaginated(int year, int month, int page, int size, String customerId, String service, String status, String date, String active);

    ReconciliationResultResponseDTO applyAdjustment(String reconciliationResultId, ApplyAdjustmentRequestDTO requestDTO);

    ReconciliationResultResponseDTO updateResult(String id, UpdateReconciliationResultRequestDTO updateReconciliationResultRequestDTO);

    void deleteResult(String id);
}
