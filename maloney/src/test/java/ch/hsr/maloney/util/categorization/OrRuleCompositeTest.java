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
public class OrRuleCompositeTest {
    private RuleComposite ruleComposite;

    private final FileAttributes testFileAttributes = new FileAttributes("testFile", "/dev/null", UUID.randomUUID(), new Date(), new Date(), new Date(), null, null);

    @Before
    public void setUp(){
        ruleComposite = new OrRuleComposite();
    }

    @Test
    public void emptyComposite(){
        boolean result = ruleComposite.match(testFileAttributes);
        Assert.assertFalse(result);
    }

    @Test
    public void trueComposite(){
        ruleComposite.addRule(CompositeTestHelper.trueRule);
        ruleComposite.addRule(CompositeTestHelper.falseRule);
        ruleComposite.addRule(CompositeTestHelper.falseRule);

        boolean result = ruleComposite.match(testFileAttributes);
        Assert.assertTrue(result);
    }

    @Test
    public void falseComposite(){
        ruleComposite.addRule(CompositeTestHelper.falseRule);
        ruleComposite.addRule(CompositeTestHelper.falseRule);
        ruleComposite.addRule(CompositeTestHelper.falseRule);

        boolean result = ruleComposite.match(testFileAttributes);
        Assert.assertFalse(result);
    }

    @Test
    public void hugeComposite(){
        CompositeTestHelper.addRules(ruleComposite, 1000, CompositeTestHelper.trueRule);

        boolean result = ruleComposite.match(testFileAttributes);
        Assert.assertTrue(result);
    }
}
