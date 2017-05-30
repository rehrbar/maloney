package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * Created by oliver on 18.05.17.
 */
public class OrRuleComposite extends RuleComposite{
    @Override
    public boolean match(FileAttributes fileAttributes) {
        for(RuleComponent rule:rules){
            if(rule.match(fileAttributes)){
                return true;
            }
        }
        return false;
    }
}
