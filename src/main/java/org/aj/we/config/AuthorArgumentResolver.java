package org.aj.we.config;

import javax.servlet.http.HttpServletRequest;
import org.aj.we.domain.Author;
import org.aj.we.service.author.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthorArgumentResolver implements HandlerMethodArgumentResolver {

  @Autowired AuthorService authorService;

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return Author.class.isAssignableFrom(methodParameter.getParameterType());
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter,
                                ModelAndViewContainer modelAndViewContainer,
                                NativeWebRequest nativeWebRequest,
                                WebDataBinderFactory webDataBinderFactory)
      throws Exception {
    HttpServletRequest request =
        nativeWebRequest.getNativeRequest(HttpServletRequest.class);
    return authorService.getAuthorFromSession(request.getSession());
  }
}
