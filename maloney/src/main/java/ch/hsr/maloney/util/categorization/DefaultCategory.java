package ch.hsr.maloney.util.categorization;

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

}
