package org.dj.we.repository;

import org.dj.we.domain.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentMongoRepository extends MongoRepository<Comment, String> {
    Comment findAllById(String id);
    List<Comment> findAllByBlogId(String blogId);
    long countByBlogId(String blogId);
}
