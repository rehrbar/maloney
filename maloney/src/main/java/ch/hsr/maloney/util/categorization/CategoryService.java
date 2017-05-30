package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by oliver on 18.05.17.
 */
public class CategoryService {
    private Map<String, Category> categories;

    public CategoryService(){
        categories = new HashMap<>();
        for(DefaultCategory defaultCategory : DefaultCategory.values()){
            categories.put(defaultCategory.getName(),CategoryFactory.getCategory(defaultCategory.getName()));
        }
    }

    Collection<Category> getCategories(){
        return categories.values();
    }

    Category getCategory(String name){
        return categories.get(name);
    }

    /**
     * Either adds the specified category to the Service or, if a Category with the same name already exists, adds the RuleComposite associated with the category to it.
     * @param category New Category with rules to be added. If the name of the category already exists, its rules will be added to the existing one instead.
     */
    public void addOrUpdateCategory(Category category){
        Category storedCategory = categories.get(category.getName());
        if(storedCategory == null){
            categories.put(category.getName(), category);
        } else {
            storedCategory.getRuleSet().addRule(category.getRuleSet());
        }
    }

    public List<Category> match(FileAttributes fileAttributes) {
        List<Category> categories = this.getCategories().stream()
                .filter(category -> category.getRuleSet().match(fileAttributes))
                .collect(Collectors.toList());
        if(categories.isEmpty()){
            categories.add(this.getCategory(DefaultCategory.UNKNOWN.getName()));
        }
        return categories;
    }
}
