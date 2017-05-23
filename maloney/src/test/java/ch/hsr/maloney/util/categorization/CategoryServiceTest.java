package ch.hsr.maloney.util.categorization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by oliver on 22.05.17.
 */
public class CategoryServiceTest {
    private CategoryService categoryService;

    @Before
    public void setUp(){
        categoryService = new CategoryService();
    }

    @Test
    public void checkDefaultCategories(){
        Assert.assertEquals(DefaultCategory.values().length, categoryService.getCategories().size());
    }

    @Test
    public void updateDefaultCategory(){
        Category category = new Category(DefaultCategory.KNOWN_GOOD.getName());
        category.addRule(RuleTestHelper.trueRule);
        categoryService.addOrUpdateCategory(category);

        category = categoryService.getCategory(DefaultCategory.KNOWN_GOOD.getName());
        Assert.assertEquals(1,category.getRuleSet().rules.size());
    }
}
