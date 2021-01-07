package org.aj.promise.config;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.aj.promise.properties.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

  // @Override
  // public void addInterceptors(InterceptorRegistry registry) {
  // registry.addInterceptor(new MyInterceptor());
  // }

  @Autowired
  AuthorArgumentResolver authorArgumentResolver;

  @Autowired
  StorageProperties properties;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(authorArgumentResolver);
  }

  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/home").setViewName("home11");
    // registry.addViewController("/").setViewName("home");
    registry.addViewController("/hello").setViewName("hello");
    registry.addViewController("/login").setViewName("login");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Objects.requireNonNull(registry);

    // could use either '/**/images/{filename:\w+\.png}' or '/**/images/*.png'
    // registry.addResourceHandler("/**/images/{filename:\\w+\\.png}").addResourceLocations("classpath:/static/")
    // .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));

    // static
    String[] dirs = { "css/", "fonts/", "images/", "js/", "scss/" };
    for (String item : dirs) {
      registry.addResourceHandler("/" + item + "**").addResourceLocations("file:static/" + item)
          .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS));
    }
    // image
    registry.addResourceHandler("/image/**").addResourceLocations(getImageLocation() + "/image/")
        .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS));

    // registry.addResourceHandler("/**/lib/*.js").addResourceLocations("classpath:/static/")
    // .setCacheControl(CacheControl.maxAge(3, TimeUnit.DAYS));
  }

  private String getImageLocation() {
    String location = properties.getLocation();
    if (!location.endsWith("/")) {
      location += "/";
    }
    return "file:" + location;
  }
}
