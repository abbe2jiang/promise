package org.aj.promise.repository;

import java.util.List;

import org.aj.promise.domain.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentMongoRepository extends MongoRepository<Comment, String> {
  Comment findAllById(String id);

  List<Comment> findAllByBlogId(String blogId);

  long countByBlogId(String blogId);
}
