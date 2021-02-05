package org.aj.promise;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.aj.promise.domain.Author;
import org.aj.promise.domain.Image;
import org.aj.promise.service.author.AuthorService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Ignore
public class InitUserTest {
  @Autowired
  private AuthorService authorService;
  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Test
  public void initUser() {
    Author author = authorService.getAuthorByUsername("admin");
    if (author == null) {
      String username = "admin";
      String password = passwordEncoder.encode("111111");
      author = Author.builder().username(username).password(password).bio(new Author.Bio()).portrait(new Image())
          .firstName("firstName").lastName("lastName").build();
      authorService.add(author);
    }
    log.info("auuthor={}", author);
  }

  public static void main(String[] args) {
    System.out.println(Type.valueOf("HTML"));

    System.out.println(Type.HTML.name());
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
