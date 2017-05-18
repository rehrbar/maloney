package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by oliver on 18.05.17.
 */
public class AndRuleComposite extends RuleComposite {
    @Override
    public boolean match(FileAttributes fileAttributes) {
        boolean isMatch;
        if(rules.size() == 0) {
            isMatch = false;
        } else {
            isMatch = true;
            Iterator<RuleComponent> iterator = rules.iterator();
            while(iterator.hasNext() && isMatch){
                RuleComponent ruleComponent = iterator.next();
                isMatch = ruleComponent.match(fileAttributes);
            }
        }
        return isMatch;
    }

}
