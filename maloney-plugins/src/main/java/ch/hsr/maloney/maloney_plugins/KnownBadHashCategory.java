package ch.hsr.maloney.maloney_plugins;

import ch.hsr.maloney.storage.hash.HashType;
import ch.hsr.maloney.util.categorization.*;

/**
 * @author onietlis
 *
 * Extends the default Category of "Known Bad" to include the rules to identify the "Known Good" artifacts of
 * the IdentifyKnownFilesJob.
 */
public class KnownBadHashCategory implements Category {
    @Override
    public String getName() {
        return DefaultCategory.KNOWN_BAD;
    }

    @Override
    public RuleComposite getRuleSet() {
        RuleComposite ruleComposite = new OrRuleComposite();
        ruleComposite.addRule(fileAttributes -> fileAttributes.getArtifacts().stream().filter(artifact ->
                artifact.getOriginator().equals(IdentifyKnownFilesJob.JOB_NAME) &&
                        artifact.getType().contains("$type") &&
                        artifact.getValue().equals(HashType.BAD)).count() > 0);
        return ruleComposite;
    }
}
