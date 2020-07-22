package org.dj.we.service.blog;

import org.dj.we.controller.Pagination;
import org.dj.we.domain.Blog;
import org.dj.we.repository.BlogMongoRepository;
import org.dj.we.repository.CommentMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class BlogService {
    @Autowired
    BlogMongoRepository blogMongoRepository;

    @Autowired
    CommentMongoRepository commentMongoRepository;

    @Autowired
    private MongoTemplate template;

    public Blog add(Blog blog) {
        return blogMongoRepository.save(blog);
    }

    public Pagination<Blog> getBlogs(Pageable pageable) {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "time"));
        return basePagination(pageable,query);
    }

    public Pagination<Blog> getBlogsByAuthorId(Pageable pageable,String authorId) {
        Criteria criteria = Criteria.where("authorId").is(authorId);
        Query query = Query.query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "time"));
        return basePagination(pageable,query);
    }

    public Pagination<Blog> getBlogsByCategoryId(Pageable pageable,String categoryId) {
        Criteria criteria = Criteria.where("categoryId").is(categoryId);
        Query query = Query.query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "time"));
        return basePagination(pageable,query);
    }

    private Pagination<Blog> basePagination(Pageable pageable,Query query){
        List<Blog> blogs = template.find(query.with(pageable), Blog.class);
        long total = template.count(query, Blog.class);
        return Pagination.of(blogs, pageable, total);
    }

    private static Random random = new Random();
    public List<Blog> getPopularBlogs(int limit) {
        Query query = new Query();
        long total = template.count(query, Blog.class);
        int page = random.nextInt((int)total/limit+1);
        Pageable pageable = PageRequest.of(page , limit);
        return template.find(query.with(pageable), Blog.class);
    }

    public List<Blog> getRelatedBlogs(String authorId,int limit) {
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
        template.updateFirst(Query.query(Criteria.where("id").is(blogId)), Update.update("comments", count), Blog.class);
    }

    public void updateCategory(String sourceCategoryId,String targetCategoryId) {
        template.updateMulti(Query.query(Criteria.where("categoryId").is(sourceCategoryId)), Update.update("categoryId", targetCategoryId), Blog.class);
    }
}
