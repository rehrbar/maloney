package ch.hsr.maloney.util.categorization;

/**
 * Created by oliver on 18.05.17.
 */
class CategoryFactory {
    static Category getCategory(String name){
        return new Category() {
            OrRuleComposite rules = new OrRuleComposite();

            @Override
            public String getName() {
                return name;
            }

            @Override
            public RuleComposite getRules() {
                return rules;
            }
        };
    }
}
