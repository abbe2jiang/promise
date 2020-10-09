package org.aj.we.repository;

import org.aj.we.domain.Author;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuthorMongoRepository extends MongoRepository<Author, String> {
  Author findAllById(String id);
  Author findAllByUsername(String username);
}
