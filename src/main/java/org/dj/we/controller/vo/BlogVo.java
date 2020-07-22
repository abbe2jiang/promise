package org.dj.we.controller.vo;

import lombok.Data;
import org.dj.we.domain.Author;
import org.dj.we.domain.Blog;
import org.dj.we.domain.Category;
import org.dj.we.domain.Image;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static DateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY");

    public static BlogVo of(Blog blog, Author author, Category category) {
        return of(blog,author,category,false);
    }

    public static BlogVo of(Blog blog, Author author, Category category,Boolean simplified ) {
        BlogVo vo = new BlogVo();
        vo.id = blog.getId();
        vo.author = author;
        vo.category = category;
        vo.profile = blog.getProfile();
        vo.title = blog.getTitle();
        vo.comments = blog.getComments();
        vo.date = dateFormat.format(new Date(blog.getTime()));
        if (!simplified){
            vo.content = blog.getContent();
        }
        return vo;
    }

}
