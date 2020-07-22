package org.dj.we.repository;

import org.dj.we.domain.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CategoryMongoRepository extends MongoRepository<Category, String> {
    List<Category> findAllByAuthorId(String author);
    Category findAllById(String id);
}
