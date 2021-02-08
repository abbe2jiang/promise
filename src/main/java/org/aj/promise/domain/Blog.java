package org.aj.promise.domain;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
public class Blog {
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

  private Type type;

  public void setType(String type) {
    this.type = Type.of(type);
  }

  public Type getType() {
    if (type == null) {
      return Type.HTML;
    }
    return type;
  }

  public enum Type {
    HTML("html"), MD("md");

    Type(String val) {
      this.value = val;
    }

    private String value;

    public String value() {
      return value;
    }

    public static Type of(String val) {
      return map.getOrDefault(val, Type.HTML);
    }

    static Map<String, Type> map = new HashMap<>();
    static {
      for (Type item : Type.values()) {
        map.put(item.value, item);
      }
    }
  }

}
