package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * @author onietlis
 *
 *
 */
public class Categorizer {
    public boolean match(FileAttributes fileAttributes, Category category) {
        return category.getRuleSet().match(fileAttributes);
    }
}
