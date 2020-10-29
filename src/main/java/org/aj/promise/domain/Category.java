package org.aj.promise.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class Category {
  @Id
  private String id;
  private String authorId;
  private String name;
  private long count;
}
