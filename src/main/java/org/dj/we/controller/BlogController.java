package org.dj.we.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dj.we.controller.vo.BlogVo;
import org.dj.we.domain.Author;
import org.dj.we.domain.Blog;
import org.dj.we.domain.Category;
import org.dj.we.domain.Image;
import org.dj.we.service.author.AuthorService;
import org.dj.we.service.blog.BlogService;
import org.dj.we.service.category.CategoryService;
import org.dj.we.service.image.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class BlogController {

    @Autowired
    BlogService blogService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AuthorService authorService;

    @Autowired
    ImageService imageService;


    @ModelAttribute("popularBlogs")
    public List<BlogVo> getPopularBlogs() {
        List<Blog> popularBlogs = blogService.getPopularBlogs(3);
        return blogVoOf(popularBlogs);
    }

    @ModelAttribute("myself")
    public Author myself(Author myself) {
        return myself;
    }

    @PostMapping("/blog")
    @ResponseBody
    public Response addBlog(@RequestBody BlogRequest blogRequest, Author user) {
        Category category = categoryService.getOrDefault(blogRequest.category, getDefaultCategory(user));

        String url = blogRequest.profile;
        Image profile = Image.builder().url(url).build();

        String compressUrl = imageService.thumbnail(url,0.5);
        if(compressUrl!=null){
            profile.setCompressUrl(compressUrl);
        }

        Blog blog = Blog.builder()
                .authorId(user.getId())
                .profile(profile)
                .categoryId(category.getId())
                .title(blogRequest.title)
                .content(blogRequest.content)
                .time(System.currentTimeMillis())
                .build();
        blogService.add(blog);
        categoryService.updateCount(category.getId());
        return Response.succeed(null);
    }


    @GetMapping("/blogs/{page:\\d+}")
    @ResponseBody
    public Response getBlogs(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page - 1, 8);
        Pagination<Blog> pagination = blogService.getBlogs(pageable);
        List<BlogVo> data = blogVoOf(pagination.getData());
        return Response.succeed(pagination.updateData(data));
    }

    @GetMapping("/blogs/user/{userId:\\w+}/{page:\\d+}")
    @ResponseBody
    public Response getBlogsByUser(@PathVariable String userId, @PathVariable int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);
        Pagination<Blog> pagination = blogService.getBlogsByAuthorId(pageable, userId);
        List<BlogVo> data = blogVoOf(pagination.getData());
        return Response.succeed(pagination.updateData(data));
    }

    @GetMapping("/blogs/category/{categoryId:\\w+}/{page:\\d+}")
    @ResponseBody
    public Response getBlogsByCategory(@PathVariable String categoryId, @PathVariable int page) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Pagination<Blog> pagination = blogService.getBlogsByCategoryId(pageable, categoryId);
        List<BlogVo> data = blogVoOf(pagination.getData());
        return Response.succeed(pagination.updateData(data));
    }



    private List<BlogVo> blogVoOf(List<Blog> blogs) {
        return blogVoOf(blogs,true);
    }

    private List<BlogVo> blogVoOf(List<Blog> blogs,Boolean simplified) {
        List<String> authorIds = new ArrayList<>();
        List<String> categoryIds = new ArrayList<>();
        for (Blog blog : blogs) {
            authorIds.add(blog.getAuthorId());
            categoryIds.add(blog.getCategoryId());
        }
        List<Author> authors = authorService.getAuthors(authorIds);
        List<Category> categories = categoryService.getCategories(categoryIds);

        Map<String, Author> authorMap = authors.stream().collect(Collectors.toMap(Author::getId, a -> a));
        Map<String, Category> categoryMap = categories.stream().collect(Collectors.toMap(Category::getId, a -> a));
        return blogs.stream().map(blog -> {
            Author author = authorMap.get(blog.getAuthorId());
            Category category = categoryMap.get(blog.getCategoryId());
            return BlogVo.of(blog, author, category, simplified);
        }).collect(Collectors.toList());
    }


    private Category getDefaultCategory(Author user) {
        return Category.builder()
                .id(user.getId())
                .authorId(user.getId())
                .name("Default")
                .count(0)
                .build();
    }

    @Data
    static class BlogRequest {
        String profile;
        String category;
        String title;
        String content;
    }

    @GetMapping("/")
    public String home(Model model, Author user) {
        commonSidebarModel(model, user);

        Pageable pageable = PageRequest.of(0, 3);
        Pagination<Blog> pagination = blogService.getBlogs(pageable);
        model.addAttribute("homeSliders",blogVoOf(pagination.getData()));
        return "home";
    }

    private void commonSidebarModel(Model model, Author user) {
        List<Category> categories = categoryService.getCategoryByAuthorId(user.getId());
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
    }

    @GetMapping("/user/{id:\\w+}")
    public String userInfo(Model model, @PathVariable String id, Author user) {
        Author author = authorService.getAuthorById(id);
        commonSidebarModel(model, author);
        model.addAttribute("editFlag", author.getId().equals(user.getId()));
        return "user";
    }

    private static Random random = new Random();

    @GetMapping("/blog-edit")
    public String blog(Model model, Author user) {
        commonSidebarModel(model, user);

        int num = random.nextInt(20);
        String url = String.format("/image/temp/%d.jpeg", num);
        model.addAttribute("profileImage", url);

        Date now = new Date();
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);
        String dateTime = date + " at " + time;
        model.addAttribute("tempTitle", dateTime);
        model.addAttribute("tempContent", dateTime);

        return "blog-edit";
    }

    private static DateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY");
    private static DateFormat timeFormat = new SimpleDateFormat("h:mm a");

    @GetMapping("/blog/{id:\\w+}")
    public String showBlog(Model model, @PathVariable String id) {
        model.addAttribute("_blog_id", id);
        Blog blog = blogService.getBlog(id);
        Author author = authorService.getAuthorById(blog.getAuthorId());
        Category category = categoryService.getCategoryById(blog.getCategoryId());
        model.addAttribute("blog", BlogVo.of(blog, author, category));

        List<Blog> relatedBlogs = blogService.getRelatedBlogs(author.getId(), 3);
        model.addAttribute("relatedBlogs", blogVoOf(relatedBlogs));

        commonSidebarModel(model, author);

        Date now = new Date();
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);
        model.addAttribute("commentDate", date + " at " + time);
        return "blog";
    }

    @GetMapping("/category/{id:\\w+}")
    public String showCategory(Model model, @PathVariable String id) {
        Category category = categoryService.getCategoryById(id);

        Author author = authorService.getAuthorById(category.getAuthorId());
        model.addAttribute("category", category);

        commonSidebarModel(model, author);

        return "category";
    }

    @PostMapping("/blog/{id:\\w+}")
    @ResponseBody
    public Response getBlog(@PathVariable String id) {
        Blog blog = blogService.getBlog(id);
        Author author = authorService.getAuthorById(blog.getAuthorId());
        Category category = categoryService.getCategoryById(blog.getCategoryId());
        return Response.succeed(BlogVo.of(blog, author, category));
    }
}
