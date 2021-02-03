package org.aj.promise.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Comment {
  public static String INDEX = "comment";
  public static String ID = "id";
  public static String BLOG_ID = "blogId";
  public static String CONTENT = "content";

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
