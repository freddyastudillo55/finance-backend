package finance.backend.bvnos.repository;

import finance.backend.bvnos.model.ReconciliationResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationResultRepository extends MongoRepository<ReconciliationResult, String> {

    List<ReconciliationResult> findByProcessIdAndActiveTrue(String processId);
}
