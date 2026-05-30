package finance.backend.bvnos.repository;

import finance.backend.bvnos.model.ProviderPaymentsReportDTO;
import finance.backend.bvnos.model.ReconciledPaymentsLastSixMonthsDTO;
import finance.backend.bvnos.model.SalesDetailsResponseDTO;
import finance.backend.bvnos.model.ServiceSalesPercentageResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReconciliationAnalyticsRepository {
    List<SalesDetailsResponseDTO> getSalesDetails(
            LocalDate startDate,
            LocalDate endDate
    );

    List<ServiceSalesPercentageResponseDTO> getServiceSalesPercentage(
            LocalDate startDate,
            LocalDate endDate
    );

    List<ProviderPaymentsReportDTO> getProviderPaymentsReport();

    List<ReconciledPaymentsLastSixMonthsDTO> getReconciledPaymentsLastSixMonths();
}
