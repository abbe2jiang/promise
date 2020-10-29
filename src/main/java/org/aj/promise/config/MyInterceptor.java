package org.aj.promise.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

// @Component
public class MyInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.isAuthenticated()) {
      // String name = auth.getName();
      HttpSession session = request.getSession();
      Object a = session.getAttribute("author");
      System.out.println(a);
    }
    request.setAttribute("myFirstAttribute", "MyFirstValueHere");
    return super.preHandle(request, response, handler);
  }
}
