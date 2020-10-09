package org.aj.we.repository;

import java.util.List;
import org.aj.we.domain.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentMongoRepository
    extends MongoRepository<Comment, String> {
  Comment findAllById(String id);
  List<Comment> findAllByBlogId(String blogId);
  long countByBlogId(String blogId);
}
