package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;

import java.util.List;

/**
 * Verifies whether or not a file belongs to a certain category or not
 */
public class Categorizer {
    /**
     *
     * @param fileAttributes    File which will get categorized
     * @param category          Category which file belongs to
     * @return                  True if category matches, false if not
     */
    boolean isMatch(FileAttributes fileAttributes, Category category){
        //TODO this
        FileAttributes targetFileAttributes = category.matchAttribute();
        return matchFileName(fileAttributes, targetFileAttributes);

    }

    private boolean matchFileName(FileAttributes fileAttributes, FileAttributes targetFileAttributes) {
        return targetFileAttributes.getFileName() != null && targetFileAttributes.getFileName().equals(fileAttributes.getFileName());
    }

    private boolean matchFilePath(FileAttributes fileAttributes, FileAttributes targetFileAttributes) {
        return targetFileAttributes.getFilePath() != null && targetFileAttributes.getFilePath().equals(fileAttributes.getFilePath());
    }

    private boolean matchDateChanged(FileAttributes fileAttributes, FileAttributes targetFileAttributes) {
        return targetFileAttributes.getDateChanged() != null && targetFileAttributes.getDateChanged().equals(fileAttributes.getDateChanged());
    }

    private boolean matchDateCreated(FileAttributes fileAttributes, FileAttributes targetFileAttributes) {
        return targetFileAttributes.getDateCreated() != null && targetFileAttributes.getDateCreated().equals(fileAttributes.getDateCreated());
    }

    private boolean matchDateAccessed(FileAttributes fileAttributes, FileAttributes targetFileAttributes) {
        return targetFileAttributes.getDateAccessed() != null && targetFileAttributes.getDateAccessed().equals(fileAttributes.getDateAccessed());
    }

    private boolean matchArtifacts(List<Artifact> artifacts, Artifact targetArtifact){
        //TODO this
        return false;
    }
}
