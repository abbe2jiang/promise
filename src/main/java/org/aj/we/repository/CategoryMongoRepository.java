package org.aj.we.repository;

import java.util.List;
import org.aj.we.domain.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryMongoRepository
    extends MongoRepository<Category, String> {
  List<Category> findAllByAuthorId(String author);
  Category findAllById(String id);
}
