package org.aj.promise.controller.vo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    vo.date = getFormatDate(blog.getTime());
    if (!simplified) {
      vo.content = blog.getContent();
    }
    return vo;
  }

  private static DateFormat dateFormat = new SimpleDateFormat("YY.MM.dd");

  public static String getFormatDate(long time) {
    Date date = new Date(time);
    return dateFormat.format(date) + " " + getWeek(date);
  }

  public static String getWeek(Date date) {
    String[] weeks = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
    if (week_index < 0) {
      week_index = 0;
    }
    return weeks[week_index];
  }
}
