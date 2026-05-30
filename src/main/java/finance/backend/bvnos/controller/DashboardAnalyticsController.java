package finance.backend.bvnos.controller;

import finance.backend.bvnos.model.ProviderPaymentsReportDTO;
import finance.backend.bvnos.model.ReconciledPaymentsLastSixMonthsDTO;
import finance.backend.bvnos.model.SalesDetailsResponseDTO;
import finance.backend.bvnos.model.ServiceSalesPercentageResponseDTO;
import finance.backend.bvnos.service.DashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardAnalyticsController {

    private final DashboardAnalyticsService dashboardAnalyticsService;

    @GetMapping("/sales-details")
    public List<SalesDetailsResponseDTO> getSalesDetails(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return dashboardAnalyticsService.getSalesDetails(startDate, endDate);
    }

    @GetMapping("/service-sales-percentage")
    public List<ServiceSalesPercentageResponseDTO> getServiceSalesPercentage(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {

        return dashboardAnalyticsService.getServiceSalesPercentage(
                startDate,
                endDate
        );
    }

    @GetMapping("/provider-payments")
    public List<ProviderPaymentsReportDTO> getProviderPaymentsReport() {
        return dashboardAnalyticsService.getProviderPaymentsReport();
    }

    @GetMapping("/reconciled-payments-last-six-months")
    public List<ReconciledPaymentsLastSixMonthsDTO> getReconciledPaymentsLastSixMonths() {
        return dashboardAnalyticsService.getReconciledPaymentsLastSixMonths();
    }

}
