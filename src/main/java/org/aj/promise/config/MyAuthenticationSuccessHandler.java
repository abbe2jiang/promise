package org.aj.promise.config;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aj.promise.domain.Author;
import org.aj.promise.domain.OperationLog;
import org.aj.promise.service.author.AuthorService;
import org.aj.promise.service.log.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Autowired
  AuthorService authorService;

  @Autowired
  OperationLogService logService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    HttpSession session = request.getSession();

    authorService.setAuthorInSession(session);
    Author author = authorService.getAuthorFromSession(session);
    logService.addLoginLog(author);
    response.sendRedirect("/");
  }
}
