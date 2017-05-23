package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * Created by oliver on 18.05.17.
 */
public class Category implements RuleComponent {

    private String name;
    private RuleComposite categoryRules;

    Category(String name, RuleComposite rules){
        this.name = name;
        if(rules == null){
            this.categoryRules = new OrRuleComposite();
        } else {
            this.categoryRules = rules;
        }
    }

    Category(String name){
        this(name, null);
    }

    public String getName(){
        return name;
    }

    public RuleComposite getRuleSet(){
        return categoryRules;
    }

    public void addRule(RuleComponent ruleComponent){
        categoryRules.addRule(ruleComponent);
    }

    public void removeRule(RuleComponent ruleComponent){
        categoryRules.removeRule(ruleComponent);
    }

    @Override
    public boolean match(FileAttributes fileAttributes) {
        return categoryRules.match(fileAttributes);
    }
}
