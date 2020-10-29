package org.aj.promise.repository;

import org.aj.promise.domain.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BlogMongoRepository extends MongoRepository<Blog, String> {
  Blog findAllById(String id);

  long countByCategoryId(String categoryId);
}
