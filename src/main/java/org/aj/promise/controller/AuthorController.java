package org.aj.promise.controller;

import javax.servlet.http.HttpSession;
import lombok.Data;

import org.aj.promise.domain.Author;
import org.aj.promise.service.author.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthorController {
  @Autowired
  AuthorService authorService;

  @PostMapping("/author")
  @ResponseBody
  public Response<Object> updateAuthor(@RequestBody AuthorRequest request, Author user, HttpSession session) {
    authorService.updateByUsername(user.getUsername(), request.portrait, request.firstName, request.lastName,
        request.brief);
    authorService.setAuthorInSession(session);
    return Response.succeed(null);
  }

  @Data
  static class AuthorRequest {
    String portrait;
    String firstName;
    String lastName;
    String brief;
  }

  @PostMapping("/author/bio")
  @ResponseBody
  public Response<Object> updateAuthorBio(@RequestBody BioRequest request, Author user, HttpSession session) {
    authorService.updateBioByUsername(user.getUsername(), request.title, request.content);
    authorService.setAuthorInSession(session);
    return Response.succeed(null);
  }

  @Data
  static class BioRequest {
    String title;
    String content;
  }
}
