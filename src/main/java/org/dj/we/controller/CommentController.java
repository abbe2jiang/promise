package org.dj.we.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dj.we.controller.vo.CommentVo;
import org.dj.we.domain.Author;
import org.dj.we.domain.Blog;
import org.dj.we.domain.Comment;
import org.dj.we.service.author.AuthorService;
import org.dj.we.service.blog.BlogService;
import org.dj.we.service.comment.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
    public Response addComment(@RequestBody CommentRequest commentRequest, Author user) {
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

        Comment comment = Comment.builder()
                .authorId(user.getId())
                .blogId(commentRequest.getBlogId())
                .content(commentRequest.content)
                .reply(Comment.createReply(replyComment, replyAuthor))
                .time(System.currentTimeMillis())
                .build();
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
    public Response getComments(@PathVariable String blogId) {
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
