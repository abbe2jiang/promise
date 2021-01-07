package org.aj.promise.service.author;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpSession;

import org.aj.promise.domain.Author;
import org.aj.promise.domain.Image;
import org.aj.promise.repository.AuthorMongoRepository;
import org.aj.promise.service.image.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthorService {

  @Autowired
  private MongoTemplate template;

  @Autowired
  AuthorMongoRepository authorMongoRepository;

  @Autowired
  ImageService imageService;

  public Author add(Author author) {
    return authorMongoRepository.save(author);
  }

  public Author getAuthorById(String id) {
    return authorMongoRepository.findAllById(id);
  }

  public List<Author> getAuthors(List<String> ids) {
    Criteria criteria = Criteria.where("id").in(ids);
    Query query = Query.query(criteria);
    return template.find(query, Author.class);
  }

  public Author getAuthorByUsername(String username) {
    return authorMongoRepository.findAllByUsername(username);
  }

  public void updateByUsername(String username, String portrait, String firstName, String lastName, String brief) {
    Image image = Image.builder().url(portrait).build();
    try {
      String compressUrl = imageService.thumbnail(portrait, 0.25);
      if (compressUrl != null) {
        image.setCompressUrl(compressUrl);
      }
    } catch (IOException e) {
      log.info("updateByUsername thumbnail error,username={},portrait{}", username, portrait, e);
    }

    Update update = Update.update("portrait", image).set("firstName", firstName).set("lastName", lastName)
        .set("brief", brief).set("updateTime", System.currentTimeMillis());
    template.updateFirst(Query.query(Criteria.where("username").is(username)), update, Author.class);
  }

  public void updateBioByUsername(String username, String title, String content) {
    Author.Bio bio = new Author.Bio();
    bio.setTitle(title);
    bio.setContent(content);
    Update update = Update.update("bio", bio).set("updateTime", System.currentTimeMillis());
    template.updateFirst(Query.query(Criteria.where("username").is(username)), update, Author.class);
  }

  public void setAuthorInSession(HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.isAuthenticated()) {
      String name = auth.getName();
      session.setAttribute(Author.class.getName(), getAuthorByUsername(name));
    }
  }

  public Author getAuthorFromSession(HttpSession session) {
    Object obj = session.getAttribute(Author.class.getName());
    return obj instanceof Author ? (Author) obj : null;
  }
}
