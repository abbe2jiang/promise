package org.dj.we.service.comment;

import org.dj.we.domain.Comment;
import org.dj.we.repository.CommentMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentMongoRepository commentMongoRepository;


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
