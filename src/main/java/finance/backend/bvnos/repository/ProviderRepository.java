package finance.backend.bvnos.repository;

import finance.backend.bvnos.model.Provider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderRepository extends MongoRepository<Provider, String> {

    List<Provider> findByActiveTrue();
}
