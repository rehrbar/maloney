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
        Assert.assertEquals(1,category.getRules().rules.size());
    }

    @Test
    public void categorizeUnknown(){
        List<Category> categories = categoryService.match(RuleTestHelper.testFileAttributes);

        Assert.assertEquals(1, categories.size());
    }

    @Test
    public void categorizeKnownGood() {
        addRuleToCategory(DefaultCategory.KNOWN_GOOD.getName(), RuleTestHelper.matchTestFileAttributes);

        List<Category> categories = categoryService.match(RuleTestHelper.testFileAttributes);

        Assert.assertEquals(1, categories.size());
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_GOOD.getName())));
    }

    private void addRuleToCategory(String categoryName, RuleComponent ruleComponent) {
        Category category = new Category() {
            @Override
            public String getName() {
                return categoryName;
            }

            @Override
            public RuleComposite getRules() {
                RuleComposite newRules = new AndRuleComposite();
                newRules.addRule(ruleComponent);
                return newRules;
            }
        };
        categoryService.addOrUpdateCategory(category);
    }

    @Test
    public void multipleCategoriesMatch(){
        addRuleToCategory(DefaultCategory.KNOWN_GOOD.getName(), RuleTestHelper.matchTestFileAttributes);
        addRuleToCategory(DefaultCategory.KNOWN_BAD.getName(), RuleTestHelper.matchTestFileAttributes);

        List<Category> categories = categoryService.match(RuleTestHelper.testFileAttributes);

        Assert.assertEquals(2, categories.size());
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_GOOD.getName())));
        Assert.assertTrue(categories.contains(categoryService.getCategory(DefaultCategory.KNOWN_BAD.getName())));
    }

}
