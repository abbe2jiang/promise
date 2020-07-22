package org.dj.we.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
public class Author {
    @Id
    private String id;
    @Indexed(
        unique = true,
        sparse = true
    )
    private String username;
    private String password;
    private String fullName;
    private String firstName;
    private String lastName;
    private String brief;
    private Bio bio;
    private Image portrait;
    private long createdTime;
    private long updateTime;

    public String getFullName() {
        return fullName == null ? firstName + " " + lastName : fullName;
    }

    @Data
    public static class Bio {
        private String title;
        private String content;
    }

}
