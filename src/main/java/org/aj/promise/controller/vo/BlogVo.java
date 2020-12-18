package org.aj.promise.controller.vo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.aj.promise.domain.Author;
import org.aj.promise.domain.Blog;
import org.aj.promise.domain.Category;
import org.aj.promise.domain.Image;

import lombok.Data;

@Data
public class BlogVo {
  private String id;
  private Author author;
  private Category category;
  private Image profile;
  private String title;
  private String content;
  private long comments;
  private String date;

  private static DateFormat dateFormat = new SimpleDateFormat("YYYY年MM月dd日");

  public static BlogVo of(Blog blog, Author author, Category category) {
    return of(blog, author, category, false);
  }

  public static BlogVo of(Blog blog, Author author, Category category, Boolean simplified) {
    BlogVo vo = new BlogVo();
    vo.id = blog.getId();
    vo.author = author;
    vo.category = category;
    vo.profile = blog.getProfile();
    vo.title = blog.getTitle();
    vo.comments = blog.getComments();
    vo.date = dateFormat.format(new Date(blog.getTime()));
    if (!simplified) {
      vo.content = blog.getContent();
    }
    return vo;
  }
}
