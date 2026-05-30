package finance.backend.bvnos.repository;

import com.mongodb.BasicDBObject;
import finance.backend.bvnos.model.ProviderPaymentsReportDTO;
import finance.backend.bvnos.model.ReconciledPaymentsLastSixMonthsDTO;
import finance.backend.bvnos.model.SalesDetailsResponseDTO;
import finance.backend.bvnos.model.ServiceSalesPercentageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
@RequiredArgsConstructor
public class ReconciliationAnalyticsRepositoryImpl implements ReconciliationAnalyticsRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<SalesDetailsResponseDTO> getSalesDetails(
            LocalDate startDate,
            LocalDate endDate
    ) {

        MatchOperation match = match(
                Criteria.where("status").is("RECONCILED")
                        .and("active").is(true)
                        .and("reconciledAt")
                        .gte(startDate.atStartOfDay())
                        .lte(endDate.atTime(23, 59, 59))
        );

        ProjectionOperation project = project()
                .andExpression("dateToString('%Y-%m-%d', reconciledAt)")
                .as("date")
                .and(ConvertOperators.ToDouble.toDouble("$finalAmount"))
                .as("finalAmount");

        GroupOperation group = group("date")
                .sum("finalAmount").as("rawTotalSales")
                .count().as("totalTransactions");

        ProjectionOperation responseProjection = project()
                .and("_id").as("date")
                .and(
                        ArithmeticOperators.Round
                                .roundValueOf("rawTotalSales")
                                .place(2)
                )
                .as("totalSales")

                .and("totalTransactions")
                .as("totalTransactions");

        SortOperation sort = sort(Sort.Direction.ASC, "date");

        Aggregation aggregation = newAggregation(
                match,
                project,
                group,
                responseProjection,
                sort
        );

        return mongoTemplate.aggregate(
                aggregation,
                "reconciliation_results",
                SalesDetailsResponseDTO.class
        ).getMappedResults();
    }

    @Override
    public List<ServiceSalesPercentageResponseDTO> getServiceSalesPercentage(
            LocalDate startDate,
            LocalDate endDate
    ) {

        MatchOperation match = match(
                Criteria.where("status").is("RECONCILED")
                        .and("active").is(true)
                        .and("reconciledAt")
                        .gte(startDate.atStartOfDay())
                        .lte(endDate.atTime(23, 59, 59))
        );

        GroupOperation group = group("service")
                .count()
                .as("totalSales");

        GroupOperation totalGroup = group()
                .sum("totalSales")
                .as("grandTotal")
                .push(
                        new BasicDBObject("service", "$_id")
                                .append("totalSales", "$totalSales")
                )
                .as("services");

        UnwindOperation unwind = unwind("services");

        ProjectionOperation project = project()
                .and("services.service")
                .as("service")

                .and("services.totalSales")
                .as("totalSales")

                .and(
                        ArithmeticOperators.Round.roundValueOf(
                                ArithmeticOperators.Multiply.valueOf(
                                        ArithmeticOperators.Divide.valueOf(
                                                "services.totalSales"
                                        ).divideBy("grandTotal")
                                ).multiplyBy(100)
                        ).place(2)
                )
                .as("percentage");

        SortOperation sort = sort(
                Sort.Direction.DESC,
                "totalSales"
        );

        Aggregation aggregation = newAggregation(
                match,
                group,
                totalGroup,
                unwind,
                project,
                sort
        );

        return mongoTemplate.aggregate(
                aggregation,
                "reconciliation_results",
                ServiceSalesPercentageResponseDTO.class
        ).getMappedResults();
    }

    @Override
    public List<ProviderPaymentsReportDTO> getProviderPaymentsReport() {

        MatchOperation match = match(
                Criteria.where("active").is(true)
        );

        ProjectionOperation project = project()
                .and("providerName")
                .as("providerName")

                .and(
                        ConvertOperators.ToDouble.toDouble("$amount")
                )
                .as("amount");

        GroupOperation group = group("providerName")
                .sum("amount")
                .as("rawTotal");

        ProjectionOperation responseProjection = project()
                .and("_id")
                .as("providerName")

                .and(
                        ArithmeticOperators.Round
                                .roundValueOf("rawTotal")
                                .place(2)
                )
                .as("totalAmount");

        SortOperation sort = sort(
                Sort.Direction.DESC,
                "totalAmount"
        );

        Aggregation aggregation = newAggregation(
                match,
                project,
                group,
                responseProjection,
                sort
        );

        return mongoTemplate.aggregate(
                aggregation,
                "provider_payments",
                ProviderPaymentsReportDTO.class
        ).getMappedResults();
    }

    @Override
    public List<ReconciledPaymentsLastSixMonthsDTO> getReconciledPaymentsLastSixMonths() {

        LocalDateTime sixMonthsAgo =
                LocalDateTime.now().minusMonths(5);

        MatchOperation match = match(
                Criteria.where("status").is("RECONCILED")
                        .and("active").is(true)
                        .and("reconciledAt").gte(sixMonthsAgo)
        );

        ProjectionOperation project = project()
                .andExpression("dateToString('%Y-%m', reconciledAt)")
                .as("month");

        GroupOperation group = group("month")
                .count()
                .as("totalReconciled");

        ProjectionOperation responseProjection = project()
                .and("_id")
                .as("month")

                .and("totalReconciled")
                .as("totalReconciled");

        SortOperation sort = sort(
                Sort.Direction.ASC,
                "month"
        );

        Aggregation aggregation = newAggregation(
                match,
                project,
                group,
                responseProjection,
                sort
        );

        return mongoTemplate.aggregate(
                aggregation,
                "reconciliation_results",
                ReconciledPaymentsLastSixMonthsDTO.class
        ).getMappedResults();
    }
}