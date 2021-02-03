package org.aj.promise.domain;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class CommonConfig {
    @Id
    private Type type;

    private String value;

    public enum Type {
        // 初始化搜索索引
        InitSearchIndex;
    }
}
