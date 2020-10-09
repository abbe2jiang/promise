package org.aj.we.service.comment;

import java.util.List;
import org.aj.we.domain.Comment;
import org.aj.we.repository.CommentMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
  @Autowired CommentMongoRepository commentMongoRepository;

  public Comment add(Comment comment) {
    return commentMongoRepository.save(comment);
  }

  public Comment getComment(String id) {
    return commentMongoRepository.findAllById(id);
  }

  public List<Comment> getComments(String blogId) {
    return commentMongoRepository.findAllByBlogId(blogId);
  }
}
