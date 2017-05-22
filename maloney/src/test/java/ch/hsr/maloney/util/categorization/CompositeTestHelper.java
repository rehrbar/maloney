package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * Created by oliver on 22.05.17.
 */
class CompositeTestHelper {
    static final RuleComponent falseRule = new RuleComponent() {
        @Override
        public boolean match(FileAttributes fileAttributes) {
            return false;
        }
    };

    static final RuleComponent trueRule = new RuleComponent() {
        @Override
        public boolean match(FileAttributes fileAttributes) {
            return true;
        }
    };

    static void addRules(RuleComposite ruleComposite, int numberOfRules, RuleComponent ruleComponent) {
        for(int i = 0; i < numberOfRules; i++){
            ruleComposite.addRule(ruleComponent);
        }
    }
}
