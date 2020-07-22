package org.dj.we.service.category;

import org.dj.we.domain.Category;
import org.dj.we.repository.BlogMongoRepository;
import org.dj.we.repository.CategoryMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CategoryService {
    @Autowired
    CategoryMongoRepository categoryMongoRepository;

    @Autowired
    BlogMongoRepository blogMongoRepository;

    @Autowired
    private MongoTemplate template;

    public Category add(Category category) {
        return categoryMongoRepository.save(category);
    }

    public void deleteById(String id) {
        categoryMongoRepository.deleteById(id);
    }

    public List<Category> getCategoryByAuthorId(String authorId) {
        return categoryMongoRepository.findAllByAuthorId(authorId);
    }

    public Category getCategoryById(String id) {
        return categoryMongoRepository.findAllById(id);
    }

    public Category getOrDefault(String id, Category defaultCategory) {
        Category category = getCategoryById(id);
        if (category == null) {
            category = add(defaultCategory);
        }
        return category;
    }

    public List<Category> getCategories(List<String> ids) {
        Criteria criteria = Criteria.where("id").in(ids);
        Query query = Query.query(criteria);
        return template.find(query, Category.class);
    }


    public void updateCount(String id) {
        long count = blogMongoRepository.countByCategoryId(id);
        template.updateFirst(Query.query(Criteria.where("id").is(id)), Update.update("count", count), Category.class);
    }
}
