package org.dj.we.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dj.we.domain.Author;
import org.dj.we.service.author.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class AuthorController {
    @Autowired
    AuthorService authorService;


    @PostMapping("/author")
    @ResponseBody
    public Response updateAuthor(@RequestBody AuthorRequest request, Author user, HttpSession session) {
        authorService.updateByUsername(user.getUsername(), request.portrait, request.firstName,
                request.lastName, request.brief);
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
    public Response updateAuthorBio(@RequestBody BioRequest request, Author user, HttpSession session) {
        authorService.updateBioByUsername(user.getUsername(), request.title,request.content);
        authorService.setAuthorInSession(session);
        return Response.succeed(null);
    }

    @Data
    static class BioRequest {
        String title;
        String content;
    }
}
