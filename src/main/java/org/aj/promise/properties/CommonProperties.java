package org.aj.promise.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("aj.common")
public class CommonProperties {
    private String lucenceIndexes = "indexDir";
    private boolean debug = false;
}
