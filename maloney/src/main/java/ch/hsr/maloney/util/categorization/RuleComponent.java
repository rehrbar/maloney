package ch.hsr.maloney.util.categorization;

import ch.hsr.maloney.storage.FileAttributes;

/**
 * Created by oliver on 18.05.17.
 */
public interface RuleComponent {
    /**
     * Determines whether or not the specified
     *
     * @param fileAttributes
     * @return
     */
    boolean match(FileAttributes fileAttributes);
}
