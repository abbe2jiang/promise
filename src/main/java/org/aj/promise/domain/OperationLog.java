package org.aj.promise.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
public class OperationLog {
  @Id
  private String id;
  private Type type;
  @Indexed
  private String authorId;
  private String value;
  private Date date;

  public enum Type {
    LOGIN, READ;
  }

  public static OperationLog of(Type type, String authorId, String value) {
    return OperationLog.builder().type(type).authorId(authorId).value(value).date(new Date()).build();
  }

  public static OperationLog of(Type type, String authorId) {
    return of(type, authorId, null);
  }

}
