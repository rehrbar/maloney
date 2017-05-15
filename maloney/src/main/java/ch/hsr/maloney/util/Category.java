package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;

/**
 * File Category, used for generating reports.
 *
 * @author onietlis
 */
public interface Category {
    /**
     *
     * @return  Get parametrized object which files of this categories should match to. Irrelevant fields are null.
     */
    FileAttributes matchAttribute();

    /**
     *
     * @return  Get parametrized object which files of this categories should match to. Irrelevant fields are null.
     */
    Artifact matchArtifact();
}
