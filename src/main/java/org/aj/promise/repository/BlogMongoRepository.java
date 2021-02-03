package org.aj.promise.repository;

import java.util.List;

import org.aj.promise.domain.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BlogMongoRepository extends MongoRepository<Blog, String> {
  Blog findAllById(String id);

  List<Blog> findAllByIdIn(List<String> ids);

  long countByCategoryId(String categoryId);
}
