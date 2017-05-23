package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.categorization.*;

/**
 * Created by oliver on 23.05.17.
 */
public class RDSCategory implements Category {
    @Override
    public String getName() {
        return DefaultCategory.KNOWN_GOOD;
    }

    @Override
    public RuleComposite getRuleSet() {
        return null;
    }

    @Override
    public void addRule(RuleComponent ruleComponent) {

    }

    @Override
    public boolean match(FileAttributes fileAttributes) {
        return false;
    }
}
