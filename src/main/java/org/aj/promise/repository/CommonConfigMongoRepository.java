package org.aj.promise.repository;

import org.aj.promise.domain.CommonConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommonConfigMongoRepository extends MongoRepository<CommonConfig, String> {
}
