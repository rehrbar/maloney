package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * Created by oliver on 23.05.17.
 */
public interface Category {
    String getName();

    RuleComposite getRuleSet();

    void addRule(RuleComponent ruleComponent);

    boolean match(FileAttributes fileAttributes);
}
