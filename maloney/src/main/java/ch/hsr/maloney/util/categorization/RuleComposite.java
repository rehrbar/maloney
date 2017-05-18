package ch.hsr.maloney.util.categorization;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by oliver on 18.05.17.
 */
abstract class RuleComposite implements RuleComponent {
    List<RuleComponent> rules;

    RuleComposite(){
        rules = new LinkedList<>();
    }

    void addRule(RuleComponent rule){
        rules.add(rule);
    }

    void removeRule(RuleComponent rule){
      rules.remove(rule);
    }
}
