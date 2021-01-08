package org.aj.promise.domain;

import lombok.Data;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
public class Video {
  @Id
  private String id;
  private String sourceUrl;
  private String compressionUrl;
  @Indexed
  private State state;
  private long createdTime;
  private long updateTime;

  public static Video of(String url) {
    Video obj = new Video();
    obj.id = UUID.randomUUID().toString();
    obj.sourceUrl = url;
    obj.state = State.Pending;
    obj.createdTime = System.currentTimeMillis();
    obj.updateTime = obj.createdTime;
    return obj;
  }

  public static enum State {
    Pending, Success, Fail
  }
}
