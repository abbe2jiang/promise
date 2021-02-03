package org.aj.promise.service.blog;

import java.util.List;
import java.util.Random;

import org.aj.promise.controller.Pagination;
import org.aj.promise.domain.Blog;
import org.aj.promise.repository.BlogMongoRepository;
import org.aj.promise.repository.CommentMongoRepository;
import org.aj.promise.service.search.LucencService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class BlogService {
  @Autowired
  BlogMongoRepository blogMongoRepository;

  @Autowired
  CommentMongoRepository commentMongoRepository;

  @Autowired
  LucencService lucencService;

  @Autowired
  private MongoTemplate template;

  public Blog add(Blog blog) {
    blog = blogMongoRepository.save(blog);
    lucencService.addBlogIndex(blog);
    return blog;
  }

  public Pagination<Blog> getBlogs(Pageable pageable) {
    Query query = new Query();
    query.with(new Sort(Sort.Direction.DESC, "updateTime"));
    return basePagination(pageable, query);
  }

  public List<Blog> searchBlogs(String s) {
    List<String>ids = lucencService.searchBlogIds(s);
    return blogMongoRepository.findAllByIdIn(ids);
  }

  public Pagination<Blog> getBlogsByAuthorId(Pageable pageable, String authorId) {
    Criteria criteria = Criteria.where("authorId").is(authorId);
    Query query = Query.query(criteria);
    query.with(new Sort(Sort.Direction.DESC, "updateTime"));
    return basePagination(pageable, query);
  }

  public Pagination<Blog> getBlogsByCategoryId(Pageable pageable, String categoryId) {
    Criteria criteria = Criteria.where("categoryId").is(categoryId);
    Query query = Query.query(criteria);
    query.with(new Sort(Sort.Direction.DESC, "updateTime"));
    return basePagination(pageable, query);
  }

  private Pagination<Blog> basePagination(Pageable pageable, Query query) {
    List<Blog> blogs = template.find(query.with(pageable), Blog.class);
    long total = template.count(query, Blog.class);
    return Pagination.of(blogs, pageable, total);
  }

  private static Random random = new Random();

  public List<Blog> getPopularBlogs(int limit) {
    Query query = new Query();
    long total = template.count(query, Blog.class);
    int page = random.nextInt((int) total / limit + 1);
    Pageable pageable = PageRequest.of(page, limit);
    return template.find(query.with(pageable), Blog.class);
  }

  public List<Blog> getRelatedBlogs(String authorId, int limit) {
    Criteria criteria = Criteria.where("authorId").is(authorId);
    Query query = new Query(criteria);
    query.with(new Sort(Sort.Direction.DESC, "time"));
    return template.find(query.limit(limit), Blog.class);
  }

  public Blog getBlog(String id) {
    return blogMongoRepository.findAllById(id);
  }

  public void updateComments(String blogId) {
    long count = commentMongoRepository.countByBlogId(blogId);
    Update update = Update.update("comments", count);
    update.set("updateTime", System.currentTimeMillis());
    template.updateFirst(Query.query(Criteria.where("id").is(blogId)), update, Blog.class);
  }

  public void updateCategory(String sourceCategoryId, String targetCategoryId) {
    template.updateMulti(Query.query(Criteria.where("categoryId").is(sourceCategoryId)),
        Update.update("categoryId", targetCategoryId), Blog.class);
  }
}
