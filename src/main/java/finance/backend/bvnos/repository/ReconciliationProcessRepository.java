package finance.backend.bvnos.repository;

import finance.backend.bvnos.model.ReconciliationProcess;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconciliationProcessRepository extends MongoRepository<ReconciliationProcess, String> {
}
