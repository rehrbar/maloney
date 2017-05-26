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
        Assert.assertEquals(3, categoryService.getCategories().size());
    }

    @Test
    public void updateDefaultCategory(){
        addRuleToCategory(DefaultCategory.KNOWN_GOOD, RuleTestHelper.trueRule);

        Category category = categoryService.getCategory(DefaultCategory.KNOWN_GOOD);
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

        addRuleToCategory(DefaultCategory.KNOWN_GOOD, RuleTestHelper.matchTestFileAttributes);

        List<Category> categories = categorizer.match(RuleTestHelper.testFileAttributes);

        Assert.assertEquals(1, categories.size());
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_GOOD)));
    }

    private void addRuleToCategory(String categoryName, RuleComponent ruleComponent) {
        DefaultCategory category = new DefaultCategory(categoryName);
        category.addRule(ruleComponent);
        categoryService.addOrUpdateCategory(category);
    }

    @Test
    public void multipleCategoriesMatch(){
        Categorizer categorizer = categoryService.getCategorizer();

        addRuleToCategory(DefaultCategory.KNOWN_GOOD, RuleTestHelper.matchTestFileAttributes);
        addRuleToCategory(DefaultCategory.KNOWN_BAD, RuleTestHelper.matchTestFileAttributes);

        List<Category> categories = categorizer.match(RuleTestHelper.testFileAttributes);

        Assert.assertEquals(2, categories.size());
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_GOOD)));
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_BAD)));
    }

}
