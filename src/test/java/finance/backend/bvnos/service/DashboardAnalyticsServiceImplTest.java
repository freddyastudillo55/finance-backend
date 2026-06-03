package finance.backend.bvnos.service;

import finance.backend.bvnos.model.ProviderPaymentsReportDTO;
import finance.backend.bvnos.model.ReconciledPaymentsLastSixMonthsDTO;
import finance.backend.bvnos.model.SalesDetailsResponseDTO;
import finance.backend.bvnos.model.ServiceSalesPercentageResponseDTO;
import finance.backend.bvnos.repository.ReconciliationAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsServiceImplTest {

    @Mock
    private ReconciliationAnalyticsRepository analyticsRepository;

    @InjectMocks
    private DashboardAnalyticsServiceImpl dashboardAnalyticsService;

    private LocalDate startDate;
    private LocalDate endDate;
    private SalesDetailsResponseDTO salesDetail;
    private ServiceSalesPercentageResponseDTO servicePercentage;
    private ProviderPaymentsReportDTO providerPaymentReport;
    private ReconciledPaymentsLastSixMonthsDTO lastSixMonths;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2025, 1, 1);
        endDate = LocalDate.of(2025, 1, 31);

        salesDetail = new SalesDetailsResponseDTO();
        salesDetail.setDate(LocalDate.of(2025, 1, 15));
        salesDetail.setTotalSales(new BigDecimal("5000.00"));
        salesDetail.setTotalTransactions(10L);

        servicePercentage = new ServiceSalesPercentageResponseDTO();
        servicePercentage.setService("INTERNET");
        servicePercentage.setTotalSales(30L);
        servicePercentage.setPercentage(60.00);

        providerPaymentReport = new ProviderPaymentsReportDTO();
        providerPaymentReport.setProviderName("Provider One");
        providerPaymentReport.setTotalAmount(10000.00);

        lastSixMonths = new ReconciledPaymentsLastSixMonthsDTO();
        lastSixMonths.setMonth("2025-01");
        lastSixMonths.setTotalReconciled(50L);
    }

    @Test
    @DisplayName("Should return sales details")
    void shouldReturnSalesDetails() {
        when(analyticsRepository.getSalesDetails(startDate, endDate))
                .thenReturn(List.of(salesDetail));

        List<SalesDetailsResponseDTO> result = dashboardAnalyticsService.getSalesDetails(startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(result.get(0).getTotalSales()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.get(0).getTotalTransactions()).isEqualTo(10L);

        verify(analyticsRepository).getSalesDetails(startDate, endDate);
    }

    @Test
    @DisplayName("Should return empty sales details when no data")
    void shouldReturnEmptySalesDetails() {
        when(analyticsRepository.getSalesDetails(startDate, endDate))
                .thenReturn(List.of());

        List<SalesDetailsResponseDTO> result = dashboardAnalyticsService.getSalesDetails(startDate, endDate);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return service sales percentages")
    void shouldReturnServiceSalesPercentages() {
        when(analyticsRepository.getServiceSalesPercentage(startDate, endDate))
                .thenReturn(List.of(servicePercentage));

        List<ServiceSalesPercentageResponseDTO> result =
                dashboardAnalyticsService.getServiceSalesPercentage(startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getService()).isEqualTo("INTERNET");
        assertThat(result.get(0).getTotalSales()).isEqualTo(30L);
        assertThat(result.get(0).getPercentage()).isEqualTo(60.00);

        verify(analyticsRepository).getServiceSalesPercentage(startDate, endDate);
    }

    @Test
    @DisplayName("Should return provider payments report")
    void shouldReturnProviderPaymentsReport() {
        when(analyticsRepository.getProviderPaymentsReport())
                .thenReturn(List.of(providerPaymentReport));

        List<ProviderPaymentsReportDTO> result = dashboardAnalyticsService.getProviderPaymentsReport();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProviderName()).isEqualTo("Provider One");
        assertThat(result.get(0).getTotalAmount()).isEqualTo(10000.00);

        verify(analyticsRepository).getProviderPaymentsReport();
    }

    @Test
    @DisplayName("Should return reconciled payments last six months")
    void shouldReturnReconciledPaymentsLastSixMonths() {
        when(analyticsRepository.getReconciledPaymentsLastSixMonths())
                .thenReturn(List.of(lastSixMonths));

        List<ReconciledPaymentsLastSixMonthsDTO> result =
                dashboardAnalyticsService.getReconciledPaymentsLastSixMonths();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMonth()).isEqualTo("2025-01");
        assertThat(result.get(0).getTotalReconciled()).isEqualTo(50L);

        verify(analyticsRepository).getReconciledPaymentsLastSixMonths();
    }

    @Test
    @DisplayName("Should delegate all calls correctly with different date ranges")
    void shouldDelegateWithDifferentDateRanges() {
        LocalDate customStart = LocalDate.of(2024, 6, 1);
        LocalDate customEnd = LocalDate.of(2024, 12, 31);

        when(analyticsRepository.getSalesDetails(customStart, customEnd)).thenReturn(List.of());
        when(analyticsRepository.getServiceSalesPercentage(customStart, customEnd)).thenReturn(List.of());

        dashboardAnalyticsService.getSalesDetails(customStart, customEnd);
        dashboardAnalyticsService.getServiceSalesPercentage(customStart, customEnd);

        verify(analyticsRepository).getSalesDetails(customStart, customEnd);
        verify(analyticsRepository).getServiceSalesPercentage(customStart, customEnd);
    }
}
