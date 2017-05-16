package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;

import java.util.List;

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
    List<FileAttributes> matchFileAttributes();

    /**
     *
     * @return  Get parametrized object which files of this categories should match to. Irrelevant fields are null.
     */
    List<Artifact> matchArtifact();
}
