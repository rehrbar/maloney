package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;

import java.util.Iterator;
import java.util.List;

/**
 * Verifies whether or not a file belongs to a certain category or not
 */
public class Categorizer {
    /**
     * @param fileAttr File which will get categorized
     * @param category Category which file belongs to
     * @return True if one of the criteria (attributes) in a category matches, false if none do
     */
    boolean isMatch(FileAttributes fileAttr, Category category) {
        boolean isMatch = false;

        // Try to match FileAttributes
        if(category.matchFileAttributes() != null){
            Iterator<FileAttributes> fileAttributesIterator = category.matchFileAttributes().iterator();
            while (!isMatch && fileAttributesIterator.hasNext()) {
                FileAttributes target = fileAttributesIterator.next();
                isMatch = matchFileName(fileAttr, target) &&
                        matchFilePath(fileAttr, target) &&
                        matchDateCreated(fileAttr, target) &&
                        matchDateChanged(fileAttr, target) &&
                        matchDateAccessed(fileAttr, target);
            }
        }

        // Try to match Artifact(s)
        if(category.matchArtifact() != null){
            Iterator<Artifact> artifactIterator = category.matchArtifact().iterator();
            while (!isMatch && artifactIterator.hasNext()) {
                Artifact targetArtifact = artifactIterator.next();
                isMatch = fileAttr.getArtifacts().stream().filter(art -> matchArtifactOriginator(art, targetArtifact) &&
                                matchArtifactType(art, targetArtifact) &&
                                matchArtifactValue(art, targetArtifact)).count() > 0;
            }
        }

        return isMatch;
    }

    private boolean matchFileName(FileAttributes fileAttr, FileAttributes target) {
        return target.getFileName() == null || target.getFileName().equals(fileAttr.getFileName());
    }

    private boolean matchFilePath(FileAttributes fileAttr, FileAttributes target) {
        return target.getFilePath() == null || target.getFilePath().equals(fileAttr.getFilePath());
    }

    private boolean matchDateChanged(FileAttributes fileAttr, FileAttributes target) {
        return target.getDateChanged() == null || target.getDateChanged().equals(fileAttr.getDateChanged());
    }

    private boolean matchDateCreated(FileAttributes fileAttr, FileAttributes target) {
        return target.getDateCreated() == null || target.getDateCreated().equals(fileAttr.getDateCreated());
    }

    private boolean matchDateAccessed(FileAttributes fileAttr, FileAttributes target) {
        return target.getDateAccessed() == null || target.getDateAccessed().equals(fileAttr.getDateAccessed());
    }

    private boolean matchArtifactOriginator(Artifact artifact, Artifact targetArtifact) {
        return targetArtifact.getOriginator() == null || targetArtifact.getOriginator().equals(artifact.getOriginator());
    }

    private boolean matchArtifactType(Artifact artifact, Artifact targetArtifact) {
        return targetArtifact.getType() == null || targetArtifact.getType().equals(artifact.getType());
    }

    private boolean matchArtifactValue(Artifact artifact, Artifact targetArtifact) {
        return targetArtifact.getValue() == null || targetArtifact.getValue().equals(artifact.getValue());
    }
}
