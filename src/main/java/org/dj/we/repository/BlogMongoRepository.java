package org.dj.we.repository;

import org.dj.we.domain.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BlogMongoRepository extends MongoRepository<Blog, String> {
    Blog findAllById(String id);
    long countByCategoryId(String categoryId);
}
