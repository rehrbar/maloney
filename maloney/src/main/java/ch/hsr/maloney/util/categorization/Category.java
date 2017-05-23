package ch.hsr.maloney.util.categorization;

/**
 * @author onietlis
 *
 * A Category represents a classification for a file. When using the same name as a default Category and adding them
 * to the CategoryService, the defined RuleCompsite (set of rules) will be added to the preexisting RuleComposite of
 * that Category.
 *
 */
public interface Category {
    /**
     * Get the name of this Category.
     *
     * @return Name of the Category
     */
    String getName();

    /**
     * Get the RuleComposite, i.e. the qualifiers (rules), for this Category.
     *
     * @return RuleComposite which represents a set of rules to which files this category applies to.
     */
    RuleComposite getRuleSet();
}
