package org.aj.promise.repository;

import java.util.List;

import org.aj.promise.domain.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryMongoRepository extends MongoRepository<Category, String> {
  List<Category> findAllByAuthorId(String author);

  Category findAllById(String id);
}
