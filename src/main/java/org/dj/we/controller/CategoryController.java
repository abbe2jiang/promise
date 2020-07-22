package org.dj.we.controller;

import lombok.extern.slf4j.Slf4j;
import org.dj.we.domain.Author;
import org.dj.we.domain.Category;
import org.dj.we.service.blog.BlogService;
import org.dj.we.service.category.CategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.expression.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Controller
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    BlogService blogService;

    @PostMapping("/category")
    @ResponseBody
    public Response addCategory(@RequestParam("name") String name, Author user) {
        if (StringUtils.isAlphanumeric(name)) {
            List<Category> categories = categoryService.getCategoryByAuthorId(user.getId());
            if (categories.size() > 10) {
                return Response.fail("Too many categories");
            }
            Category category = Category.builder()
                    .authorId(user.getId())
                    .name(name)
                    .count(0)
                    .build();
            categoryService.add(category);
            return Response.succeed(null);
        }

        return Response.fail("Invalid name");
    }

    @DeleteMapping("/category")
    @ResponseBody
    public Response deleteCategory(@RequestParam("id") String id, Author user) {
        if (StringUtils.isAlphanumeric(id)) {
            Category category = categoryService.getCategoryById(id);
            if(category!=null && category.getCount()==0
                    && user.getId().equals(category.getAuthorId())){
                categoryService.deleteById(id);
                return Response.succeed(null);
            }
        }

        return Response.fail("Invalid id");
    }

    @PutMapping("/category")
    @ResponseBody
    public Response moveCategory(@RequestParam("sourceId") String sourceId,
                                 @RequestParam("targetId") String targetId,Author user) {
        if(sourceId.equals(targetId))
            Response.succeed(null);

        if (StringUtils.isAlphanumeric(sourceId) && StringUtils.isAlphanumeric(targetId)) {
            List<Category> categories = categoryService.getCategories(Arrays.asList(sourceId,targetId));
            if(categories.size()==2
                    && user.getId().equals(categories.get(0).getAuthorId())
                    && user.getId().equals(categories.get(1).getAuthorId())){
                blogService.updateCategory(sourceId,targetId);
                categoryService.updateCount(targetId);
                categoryService.updateCount(sourceId);
                return Response.succeed(null);
            }
        }

        return Response.fail("Invalid id");
    }

    @GetMapping("/category")
    @ResponseBody
    public Response getCategories(Author user) {
        List<Category> categories = categoryService.getCategoryByAuthorId(user.getId());
        return Response.succeed(categories);
    }

}
