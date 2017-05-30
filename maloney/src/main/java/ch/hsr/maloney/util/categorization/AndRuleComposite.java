package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * Created by oliver on 18.05.17.
 */
public class AndRuleComposite extends RuleComposite {
    @Override
    public boolean match(FileAttributes fileAttributes) {
        for(RuleComponent rule:rules){
            if(!rule.match(fileAttributes)){
                return false;
            }
        }
        // Only get to this point when all rules were a match. Lastly check whether there are any elements at all.
        return !rules.isEmpty();
    }

}
