package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author onietlis
 *
 * Returns all Categories which apply
 */
public class Categorizer {
    private CategoryService categoryService;

    Categorizer(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    public List<Category> match(FileAttributes fileAttributes) {
        List<Category> categories = categoryService.getCategories().stream()
                .filter(category -> category.match(fileAttributes))
                .collect(Collectors.toList());
        if(categories.isEmpty()){
            categories.add(categoryService.getCategory(DefaultCategory.UNKNOWN.getName()));
        }
        return categories;
    }
}
