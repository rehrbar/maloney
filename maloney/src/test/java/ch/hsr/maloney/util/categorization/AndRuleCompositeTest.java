package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

/**
 * Created by oliver on 22.05.17.
 */
public class AndRuleCompositeTest {

    private RuleComposite ruleComposite;

    private final FileAttributes testFileAttributes = new FileAttributes("testFile", "/dev/null", UUID.randomUUID(), new Date(), new Date(), new Date(), null, null);

    @Before
    public void setUp(){
        ruleComposite = new AndRuleComposite();
    }

    @Test
    public void emptyComposite(){
        boolean result = ruleComposite.match(testFileAttributes);
        Assert.assertFalse(result);
    }

    @Test
    public void trueComposite(){
        ruleComposite.addRule(RuleTestHelper.trueRule);
        ruleComposite.addRule(RuleTestHelper.trueRule);
        ruleComposite.addRule(RuleTestHelper.trueRule);

        boolean result = ruleComposite.match(testFileAttributes);
        Assert.assertTrue(result);
    }

    @Test
    public void falseComposite(){
        ruleComposite.addRule(RuleTestHelper.trueRule);
        ruleComposite.addRule(RuleTestHelper.trueRule);
        ruleComposite.addRule(RuleTestHelper.falseRule);

        boolean result = ruleComposite.match(testFileAttributes);
        Assert.assertFalse(result);
    }

    @Test
    public void hugeComposite(){
        RuleTestHelper.addRules(ruleComposite, 1000, RuleTestHelper.trueRule);

        boolean result = ruleComposite.match(testFileAttributes);
        Assert.assertTrue(result);
    }
}
