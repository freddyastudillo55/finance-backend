package finance.backend.bvnos.service;

import finance.backend.bvnos.model.ProviderPaymentsReportDTO;
import finance.backend.bvnos.model.ReconciledPaymentsLastSixMonthsDTO;
import finance.backend.bvnos.model.SalesDetailsResponseDTO;
import finance.backend.bvnos.model.ServiceSalesPercentageResponseDTO;
import finance.backend.bvnos.repository.ReconciliationAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardAnalyticsServiceImpl implements DashboardAnalyticsService{

    private final ReconciliationAnalyticsRepository analyticsRepository;

    @Override
    public List<SalesDetailsResponseDTO> getSalesDetails(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return analyticsRepository.getSalesDetails(
                startDate,
                endDate
        );
    }

    @Override
    public List<ServiceSalesPercentageResponseDTO> getServiceSalesPercentage(
            LocalDate startDate,
            LocalDate endDate
    ) {

        return analyticsRepository.getServiceSalesPercentage(
                startDate,
                endDate
        );
    }

    @Override
    public List<ProviderPaymentsReportDTO> getProviderPaymentsReport() {

        return analyticsRepository.getProviderPaymentsReport();
    }

    @Override
    public List<ReconciledPaymentsLastSixMonthsDTO> getReconciledPaymentsLastSixMonths() {
        return analyticsRepository.getReconciledPaymentsLastSixMonths();
    }

}
