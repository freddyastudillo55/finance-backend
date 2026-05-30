package finance.backend.bvnos.service;

import finance.backend.bvnos.model.ReconciliationResult;
import finance.backend.bvnos.model.ReconciliationStatus;
import finance.backend.bvnos.repository.ReconciliationResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DevSeedServiceImpl implements DevSeedService {

    private final ReconciliationResultRepository repository;

    private final Random random = new Random();

    @Override
    public void seedSalesData() {

        for (int i = 0; i < 150; i++) {

            ReconciliationResult result =
                    new ReconciliationResult();

            result.setProcessId(
                    UUID.randomUUID().toString()
            );

            result.setCustomerId(
                    "CUST-" + (1000 + i)
            );

            result.setService(
                    generateService()
            );

            double amount =
                    100 + (5000 * random.nextDouble());

            result.setFinalAmount(
                    BigDecimal.valueOf(amount)
            );

            result.setStatus(
                    ReconciliationStatus.RECONCILED
            );

            result.setDifference(
                    BigDecimal.ZERO
            );

            result.setActive(true);

            LocalDateTime randomDate =
                    LocalDateTime.now()
                            .minusDays(
                                    random.nextInt(90)
                            );

            result.setCreatedAt(randomDate);

            result.setReconciledAt(randomDate);

            repository.save(result);
        }
    }

    private String generateService() {

        String[] services = {

                "Cloud Storage",

                "Financial Reporting",

                "Payment Gateway",

                "Enterprise Analytics",

                "Premium Support"
        };

        return services[
                random.nextInt(
                        services.length
                )
                ];
    }
}
