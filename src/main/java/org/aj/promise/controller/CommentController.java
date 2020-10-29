package org.aj.promise.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;

import org.aj.promise.controller.vo.CommentVo;
import org.aj.promise.domain.Author;
import org.aj.promise.domain.Blog;
import org.aj.promise.domain.Comment;
import org.aj.promise.service.author.AuthorService;
import org.aj.promise.service.blog.BlogService;
import org.aj.promise.service.comment.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CommentController {
  @Autowired
  BlogService blogService;

  @Autowired
  AuthorService authorService;

  @Autowired
  CommentService commentService;

  @PostMapping("/comment")
  @ResponseBody
  public Response<Object> addComment(@RequestBody CommentRequest commentRequest, Author user) {
    Blog blog = blogService.getBlog(commentRequest.getBlogId());
    if (blog == null) {
      return Response.fail("error blog id");
    }
    String replyId = commentRequest.getReplyId();
    Comment replyComment = null;
    Author replyAuthor = null;
    if (!StringUtils.isEmpty(replyId)) {
      replyComment = commentService.getComment(replyId);
      if (replyComment == null) {
        return Response.fail("error comment");
      }
      replyAuthor = authorService.getAuthorById(replyComment.getAuthorId());
    }

    Comment comment = Comment.builder().authorId(user.getId()).blogId(commentRequest.getBlogId())
        .content(commentRequest.content).reply(Comment.createReply(replyComment, replyAuthor))
        .time(System.currentTimeMillis()).build();
    commentService.add(comment);
    blogService.updateComments(commentRequest.getBlogId());
    return Response.succeed(null);
  }

  @Data
  static class CommentRequest {
    private String blogId;
    private String replyId;
    private String content;
  }

  @GetMapping("/comment/{blogId:\\w+}")
  @ResponseBody
  public Response<Object> getComments(@PathVariable String blogId) {
    List<Comment> comments = commentService.getComments(blogId);

    return Response.succeed(CommentVoOf(comments));
  }

  private List<CommentVo> CommentVoOf(List<Comment> comments) {
    List<String> authorIds = new ArrayList<>();
    for (Comment comment : comments) {
      authorIds.add(comment.getAuthorId());
    }
    List<Author> authors = authorService.getAuthors(authorIds);

    Map<String, Author> authorMap = authors.stream().collect(Collectors.toMap(Author::getId, a -> a));
    return comments.stream().map(comment -> {
      Author author = authorMap.get(comment.getAuthorId());
      return CommentVo.of(comment, author);
    }).collect(Collectors.toList());
  }
}
