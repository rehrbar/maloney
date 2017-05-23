package ch.hsr.maloney.util.categorization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
        addRuleToCategory(DefaultCategory.KNOWN_GOOD.getName(), RuleTestHelper.trueRule);

        Category category = categoryService.getCategory(DefaultCategory.KNOWN_GOOD.getName());
        Assert.assertEquals(1,category.getRuleSet().rules.size());
    }

    @Test
    public void categorizeUnknown(){
        Categorizer categorizer = categoryService.getCategorizer();

        List<Category> categories = categorizer.match(RuleTestHelper.testFileAttributes);

        Assert.assertEquals(1, categories.size());
    }

    @Test
    public void categorizeKnownGood() {
        Categorizer categorizer = categoryService.getCategorizer();

        addRuleToCategory(DefaultCategory.KNOWN_GOOD.getName(), RuleTestHelper.matchTestFileAttributes);

        List<Category> categories = categorizer.match(RuleTestHelper.testFileAttributes);

        Assert.assertEquals(1, categories.size());
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_GOOD.getName())));
    }

    private void addRuleToCategory(String categoryName, RuleComponent ruleComponent) {
        Category category = new Category(categoryName);
        category.addRule(ruleComponent);
        categoryService.addOrUpdateCategory(category);
    }

    @Test
    public void multipleCategoriesMatch(){
        Categorizer categorizer = categoryService.getCategorizer();

        addRuleToCategory(DefaultCategory.KNOWN_GOOD.getName(), RuleTestHelper.matchTestFileAttributes);
        addRuleToCategory(DefaultCategory.KNOWN_BAD.getName(), RuleTestHelper.matchTestFileAttributes);

        List<Category> categories = categorizer.match(RuleTestHelper.testFileAttributes);

        Assert.assertEquals(2, categories.size());
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_GOOD.getName())));
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_BAD.getName())));
    }

}
