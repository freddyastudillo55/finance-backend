package finance.backend.bvnos.repository;

import finance.backend.bvnos.model.ProviderPayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderPaymentRepository extends MongoRepository<ProviderPayment, String> {

    List<ProviderPayment> findByActiveTrue();

    List<ProviderPayment> findByProviderIdAndActiveTrue(String providerId);
}
