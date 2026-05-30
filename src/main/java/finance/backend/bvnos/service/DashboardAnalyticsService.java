package finance.backend.bvnos.service;

import finance.backend.bvnos.model.ProviderPaymentsReportDTO;
import finance.backend.bvnos.model.ReconciledPaymentsLastSixMonthsDTO;
import finance.backend.bvnos.model.SalesDetailsResponseDTO;
import finance.backend.bvnos.model.ServiceSalesPercentageResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface DashboardAnalyticsService {
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
