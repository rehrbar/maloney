package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

import java.util.Iterator;

/**
 * Created by oliver on 18.05.17.
 */
public class OrRuleComposite extends RuleComposite{
    @Override
    public boolean match(FileAttributes fileAttributes) {
        boolean isMatch = false;
        Iterator<RuleComponent> iterator = rules.iterator();
        while(iterator.hasNext() && !isMatch){
            RuleComponent ruleComponent = iterator.next();
            isMatch = ruleComponent.match(fileAttributes);
        }
        return isMatch;
    }
}
