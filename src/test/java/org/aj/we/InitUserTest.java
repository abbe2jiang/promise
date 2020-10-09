package org.aj.we;

import lombok.extern.slf4j.Slf4j;
import org.aj.we.domain.Author;
import org.aj.we.domain.Image;
import org.aj.we.service.author.AuthorService;
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
  @Autowired private AuthorService authorService;
  @Autowired private BCryptPasswordEncoder passwordEncoder;

  @Test
  public void initUser() {
    Author author = authorService.getAuthorByUsername("admin");
    if (author == null) {
      String username = "admin";
      String password = passwordEncoder.encode("111111");
      author = Author.builder().username(username).password(password).bio(new Author.Bio()).portrait(new Image()).build();
      authorService.add(author);
    }
    log.info("auuthor={}", author);
  }
}
