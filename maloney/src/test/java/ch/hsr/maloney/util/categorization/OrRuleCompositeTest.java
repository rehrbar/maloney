package ch.hsr.maloney.util.categorization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by oliver on 22.05.17.
 */
public class OrRuleCompositeTest {
    private RuleComposite ruleComposite;

    @Before
    public void setUp(){
        ruleComposite = new OrRuleComposite();
    }

    @Test
    public void emptyComposite(){
        Assert.assertFalse(ruleComposite.match(RuleTestHelper.testFileAttributes));
    }

    @Test
    public void trueComposite(){
        ruleComposite.addRule(RuleTestHelper.trueRule);
        ruleComposite.addRule(RuleTestHelper.falseRule);
        ruleComposite.addRule(RuleTestHelper.falseRule);

        Assert.assertTrue(ruleComposite.match(RuleTestHelper.testFileAttributes));
    }

    @Test
    public void falseComposite(){
        ruleComposite.addRule(RuleTestHelper.falseRule);
        ruleComposite.addRule(RuleTestHelper.falseRule);
        ruleComposite.addRule(RuleTestHelper.falseRule);

        Assert.assertFalse(ruleComposite.match(RuleTestHelper.testFileAttributes));
    }

    @Test
    public void hugeComposite(){
        RuleTestHelper.addRules(ruleComposite, 1000, RuleTestHelper.trueRule);

        Assert.assertTrue(ruleComposite.match(RuleTestHelper.testFileAttributes));
    }
}
