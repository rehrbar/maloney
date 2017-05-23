package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * Created by oliver on 18.05.17.
 */
public class DefaultCategory implements Category {

    public static final String KNOWN_GOOD = "Known Good";
    public static final String KNOWN_BAD = "Known Bad";
    public static final String UNKNOWN = "Unknown";

    private String name;
    private RuleComposite categoryRules;

    DefaultCategory(String name, RuleComposite rules){
        this.name = name;
        if(rules == null){
            this.categoryRules = new OrRuleComposite();
        } else {
            this.categoryRules = rules;
        }
    }

    DefaultCategory(String name){
        this(name, null);
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public RuleComposite getRuleSet(){
        return categoryRules;
    }

    public void addRule(RuleComponent ruleComponent){
        categoryRules.addRule(ruleComponent);
    }

    public boolean match(FileAttributes fileAttributes) {
        return categoryRules.match(fileAttributes);
    }
}
