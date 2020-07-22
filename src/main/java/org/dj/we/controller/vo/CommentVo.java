package org.dj.we.controller.vo;

import lombok.Data;
import org.dj.we.domain.Author;
import org.dj.we.domain.Comment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class CommentVo {
    private String id;
    private String blogId;
    private Author author;
    private String content;
    private ReplyVo reply;
    private String date;

    private static DateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY");
    private static DateFormat timeFormat = new SimpleDateFormat("h:mm a");

    public static CommentVo of(Comment comment, Author author) {
        CommentVo vo = new CommentVo();
        vo.id = comment.getId();
        vo.author = author;
        vo.blogId = comment.getBlogId();
        vo.content = comment.getContent();
        vo.reply = createReplyVo(comment);
        String date = dateFormat.format(new Date(comment.getTime()));
        String time = timeFormat.format(new Date(comment.getTime()));
        vo.date = date + " at " + time;
        return vo;
    }

    public static List<CommentVo> of(List<Comment> comments, List<Author> authors) {
        Map<String, Author> authorMap = authors.stream().collect(Collectors.toMap(Author::getId, a -> a));
        return comments.stream().map(comment -> {
            Author author = authorMap.get(comment.getAuthorId());
            return CommentVo.of(comment, author);
        }).collect(Collectors.toList());
    }

    private static ReplyVo createReplyVo(Comment comment){
        Comment.Reply reply = comment.getReply();
        if(reply==null) return null;
        ReplyVo vo = new ReplyVo();
        vo.id = reply.getId();
        vo.authorName = reply.getAuthorName();
        vo.content = reply.getContent();
        String date = dateFormat.format(new Date(reply.getTime()));
        String time = timeFormat.format(new Date(reply.getTime()));
        vo.date = date + " at " + time;
        return vo;
    }

    @Data
    public static class ReplyVo {
        private String id;
        private String authorName;
        private String content;
        private String date;
    }
}
