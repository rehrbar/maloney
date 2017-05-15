package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;

import java.util.Date;

/**
 * File Category, used for generating reports.
 *
 * @author onietlis
 */
public interface Category {
    /**
     *
     * @param fileAttributes    fileAttribute with fields which should be matched, other fields shall be null
     * @return  True when specified parameter matched, false when not
     */
    boolean matchAttribute(FileAttributes fileAttributes);

    /**
     *
     * @param artifact  artifact with fields which should be matched, other fields shall be null
     * @return  True when specified parameter matched, false when not
     */
    boolean matchArtifact(Artifact artifact);
}
