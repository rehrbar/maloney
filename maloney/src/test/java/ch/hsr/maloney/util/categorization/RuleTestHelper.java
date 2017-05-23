package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

import java.util.Date;
import java.util.UUID;

/**
 * Created by oliver on 22.05.17.
 */
class RuleTestHelper {
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

    private static final String testFileName = "testFile";

    static final FileAttributes testFileAttributes = new FileAttributes(testFileName, "/dev/null", UUID.randomUUID(), new Date(), new Date(), new Date(), null, null);

    static final RuleComponent matchTestFileAttributes = new RuleComponent() {
        @Override
        public boolean match(FileAttributes fileAttributes) {
            return fileAttributes.getFileName().equals(testFileName);
        }
    };

    static void addRules(RuleComposite ruleComposite, int numberOfRules, RuleComponent ruleComponent) {
        for(int i = 0; i < numberOfRules; i++){
            ruleComposite.addRule(ruleComponent);
        }
    }
}
