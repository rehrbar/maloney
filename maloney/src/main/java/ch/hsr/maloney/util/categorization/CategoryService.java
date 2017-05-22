package ch.hsr.maloney.util.categorization;

import java.util.*;

/**
 * Created by oliver on 18.05.17.
 */
public class CategoryService {
    private Map<String, Category> categories;

    public CategoryService(){
        categories = new HashMap<>();
        for(DefaultCategory defaultCategory : DefaultCategory.values()){
            categories.put(defaultCategory.getName(), new Category(defaultCategory.getName()));
        }
    }

    public Collection<Category> getCategories(){
        return categories.values();
    }

    public Category getCategory(String name){
        return categories.get(name);
    }

    public void addOrUpdateCategory(Category category){
        categories.put(category.getName(), category);
    }

    public Categorizer getCategorizer(){
        return new Categorizer(this);
    }
}
