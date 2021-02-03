package org.aj.promise.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
public class Blog {
  public static String INDEX = "blog";
  public static String ID = "id";
  public static String CONTENT = "content";
  @Id
  private String id;
  @Indexed
  private String authorId;
  @Indexed
  private String categoryId;
  private Image profile;
  private String title;
  private String content;
  private long comments;
  @Indexed
  private long updateTime;
  @Indexed
  private long time;
}
