package org.aj.promise.repository;

import org.aj.promise.domain.OperationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OperationLogMongoRepository extends MongoRepository<OperationLog, String> {

}
