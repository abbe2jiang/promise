package org.aj.promise.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {
  private String url;
  private String compressUrl;

  public String getCompressUrl() {
    return compressUrl == null ? url : compressUrl;
  }
}
