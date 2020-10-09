package org.aj.we.controller;

import java.util.Arrays;
import java.util.List;
import org.aj.we.domain.Author;
import org.aj.we.domain.Category;
import org.aj.we.service.blog.BlogService;
import org.aj.we.service.category.CategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class CategoryController {

  @Autowired CategoryService categoryService;

  @Autowired BlogService blogService;

  @PostMapping("/category")
  @ResponseBody
  public Response<String> addCategory(@RequestParam("name") String name,
                                      Author user) {
    if (StringUtils.isAlphanumeric(name)) {
      List<Category> categories =
          categoryService.getCategoryByAuthorId(user.getId());
      if (categories.size() > 10) {
        return Response.fail("Too many categories");
      }
      Category category =
          Category.builder().authorId(user.getId()).name(name).count(0).build();
      categoryService.add(category);
      return Response.succeed(null);
    }

    return Response.fail("Invalid name");
  }

  @DeleteMapping("/category")
  @ResponseBody
  public Response<String> deleteCategory(@RequestParam("id") String id,
                                         Author user) {
    if (StringUtils.isAlphanumeric(id)) {
      Category category = categoryService.getCategoryById(id);
      if (category != null && category.getCount() == 0 &&
          user.getId().equals(category.getAuthorId())) {
        categoryService.deleteById(id);
        return Response.succeed(null);
      }
    }

    return Response.fail("Invalid id");
  }

  @PutMapping("/category")
  @ResponseBody
  public Response<String>
  moveCategory(@RequestParam("sourceId") String sourceId,
               @RequestParam("targetId") String targetId, Author user) {
    if (sourceId.equals(targetId))
      Response.succeed(null);

    if (StringUtils.isAlphanumeric(sourceId) &&
        StringUtils.isAlphanumeric(targetId)) {
      List<Category> categories =
          categoryService.getCategories(Arrays.asList(sourceId, targetId));
      if (categories.size() == 2 &&
          user.getId().equals(categories.get(0).getAuthorId()) &&
          user.getId().equals(categories.get(1).getAuthorId())) {
        blogService.updateCategory(sourceId, targetId);
        categoryService.updateCount(targetId);
        categoryService.updateCount(sourceId);
        return Response.succeed(null);
      }
    }

    return Response.fail("Invalid id");
  }

  @GetMapping("/category")
  @ResponseBody
  public Response<List<Category>> getCategories(Author user) {
    List<Category> categories =
        categoryService.getCategoryByAuthorId(user.getId());
    return Response.succeed(categories);
  }
}
