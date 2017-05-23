package ch.hsr.maloney.util.categorization;

import java.util.Collection;

/**
 * Created by oliver on 23.05.17.
 */
public class FakeCategoryService extends CategoryService {
    public FakeCategoryService() {
        super();
    }

    @Override
    Collection<Category> getCategories() {
        return super.getCategories();
    }

    @Override
    Category getCategory(String name) {
        return super.getCategory(name);
    }

    @Override
    public void addOrUpdateCategory(Category category) {
        super.addOrUpdateCategory(category);
    }

    @Override
    public Categorizer getCategorizer() {
        return super.getCategorizer();
    }
}
