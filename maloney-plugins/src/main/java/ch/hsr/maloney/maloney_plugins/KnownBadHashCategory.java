package ch.hsr.maloney.maloney_plugins;

import ch.hsr.maloney.storage.hash.HashType;
import ch.hsr.maloney.util.categorization.*;

/**
 * @author onietlis
 *
 * Extends the default Category of "Known Bad" to include the rules to identify the "Known Bad" artifacts of
 * the IdentifyKnownFilesJob.
 */
public class KnownBadHashCategory implements Category {
    @Override
    public String getName() {
        return DefaultCategory.KNOWN_BAD.getName();
    }

    @Override
    public RuleComposite getRuleSet() {
        RuleComposite ruleComposite = new OrRuleComposite();
        ruleComposite.addRule(fileAttributes -> fileAttributes.getArtifacts().stream().anyMatch(artifact ->
                artifact.getOriginator().equals(IdentifyKnownFilesJob.JOB_NAME) &&
                        artifact.getType().contains("$type") &&
                        artifact.getValue().equals(HashType.BAD)));
        return ruleComposite;
    }
}
