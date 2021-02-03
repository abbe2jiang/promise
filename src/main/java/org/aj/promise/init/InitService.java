package org.aj.promise.init;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.aj.promise.domain.CommonConfig;
import org.aj.promise.domain.Blog;
import org.aj.promise.domain.Comment;
import org.aj.promise.repository.CommonConfigMongoRepository;
import org.aj.promise.service.search.LucencService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InitService {
    @Autowired
    CommonConfigMongoRepository commonConfigMongoRepository;

    @Autowired
    private MongoTemplate template;

    @Autowired
    LucencService lucencService;

    @PostConstruct
    public void init() {
        initSearchIndex();
    }

    private void initSearchIndex() {
        runInit(CommonConfig.Type.InitSearchIndex, () -> {
            log.info("initSearchIndex begin");
            initSearchIndexBlog();
            initSearchIndexComment();
            log.info("initSearchIndex end");
        });
    }

    private void initSearchIndexBlog() {
        log.info("initSearchIndexBlog begin");
        int num = 0;
        Query query = new Query();
        query.with(new Sort(Sort.Direction.ASC, "time"));
        int i = 0;
        while (true) {
            Pageable pageable = PageRequest.of(i, 50);
            List<Blog> blogs = template.find(query.with(pageable), Blog.class);
            if (blogs.isEmpty()) {
                break;
            }
            for (Blog blog : blogs) {
                lucencService.addBlogIndex(blog);
            }
            num += blogs.size();
            i++;
            log.info("initSearchIndexBlog num={}", num);
        }
        log.info("initSearchIndexBlog end");
    }

    private void initSearchIndexComment() {
        log.info("initSearchIndexComment begin");
        int num = 0;
        Query query = new Query();
        query.with(new Sort(Sort.Direction.ASC, "time"));
        int i = 0;
        while (true) {
            Pageable pageable = PageRequest.of(i, 50);
            List<Comment> comments = template.find(query.with(pageable), Comment.class);
            if (comments.isEmpty()) {
                break;
            }
            for (Comment comment : comments) {
                lucencService.addCommentIndex(comment);
            }
            num += comments.size();
            i++;
            log.info("initSearchIndexComment num={}", num);
        }
        log.info("initSearchIndexComment end");
    }

    private void runInit(CommonConfig.Type type, Runnable fun) {
        Optional<CommonConfig> opt = commonConfigMongoRepository.findById(type.name());
        if (opt.isPresent()) {
            return;
        }
        fun.run();
        CommonConfig config = new CommonConfig();
        config.setType(CommonConfig.Type.InitSearchIndex);
        config.setValue("true");
        commonConfigMongoRepository.save(config);
    }
}
