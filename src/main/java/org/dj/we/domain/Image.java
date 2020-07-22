package org.dj.we.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Image {
    private String url;
    private String compressUrl;

    public String getCompressUrl() {
        return compressUrl == null ? url : compressUrl;
    }
}
