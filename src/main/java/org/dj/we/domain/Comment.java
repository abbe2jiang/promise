package org.dj.we.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Comment {
    private String id;
    private String blogId;
    private String authorId;
    private String content;
    private Reply reply;
    private long time;

    public static Reply createReply(Comment comment, Author author) {
        if (comment == null)
            return null;
        Reply reply = new Reply();
        reply.id = comment.id;
        reply.authorName = author.getFirstName() + " " + author.getLastName();
        reply.content = comment.content;
        reply.time = comment.time;
        return reply;
    }

    @Data
    public static class Reply {
        private String id;
        private String authorName;
        private String content;
        private long time;
    }
}
