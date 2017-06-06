package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.util.categorization.Category;
import ch.hsr.maloney.util.categorization.DefaultCategory;
import ch.hsr.maloney.util.categorization.OrRuleComposite;
import ch.hsr.maloney.util.categorization.RuleComposite;

public class AuthenticodeCategoryKnownGood implements Category {
    @Override
    public String getName() {
        return DefaultCategory.KNOWN_GOOD.getName();
    }

    @Override
    public RuleComposite getRules() {
        RuleComposite rulecomposite = new OrRuleComposite();
        rulecomposite.addRule(fileAttributes -> fileAttributes.getArtifacts().stream().anyMatch(artifact ->
                        artifact.getOriginator().equals(AuthenticodeSignatureLookupJob.JOB_NAME)
                && artifact.getType().equals("authenticode$status")
                        && artifact.getValue().toString().toLowerCase().contains("good")
        ));
        rulecomposite.addRule(fileAttributes -> fileAttributes.getArtifacts().stream().anyMatch(artifact ->
                artifact.getOriginator().equals(AuthenticodePEJob.JOB_NAME)
                        && artifact.getType().equals("signature")
                        && artifact.getValue().toString().toLowerCase().contains("authenticode_ok")
        ));
        return rulecomposite;
    }
}
