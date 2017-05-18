package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * Created by oliver on 18.05.17.
 */
public class Category implements RuleComponent {

    private String name;
    private RuleComposite rules;

    Category(String name, RuleComposite rules){
        this.name = name;
        if(rules == null){
            rules = new OrRuleComposite();
        } else {
            this.rules = rules;
        }
    }

    Category(String name){
        this(name, null);
    }

    public String getName(){
        return name;
    }

    RuleComposite getRuleSet(){
        return rules;
    }

    void addRuleSet(RuleComponent ruleComponent){
        rules.addRule(ruleComponent);
    }

    void removeRuleSet(RuleComponent ruleComponent){
        rules.removeRule(ruleComponent);
    }

    @Override
    public boolean match(FileAttributes fileAttributes) {
        return false;
    }
}
