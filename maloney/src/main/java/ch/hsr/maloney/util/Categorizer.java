package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;

import java.util.List;

/**
 * Verifies whether or not a file belongs to a certain category or not
 */
public class Categorizer {
    /**
     * @param fileAttr File which will get categorized
     * @param category Category which file belongs to
     * @return True if category matches, false if not
     */
    boolean isMatch(FileAttributes fileAttr, Category category) {
        FileAttributes target = category.matchAttribute();
        Artifact targetArtifact = category.matchArtifact();

        return
                matchFileName(fileAttr, target) &&
                matchFilePath(fileAttr, target) &&
                matchDateCreated(fileAttr, target) &&
                matchDateChanged(fileAttr, target) &&
                matchDateAccessed(fileAttr, target) &&
                matchArtifacts(fileAttr.getArtifacts(), targetArtifact);

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

    private boolean matchArtifactOriginator(Artifact artifact, Artifact targetArtifact){
        return targetArtifact.getOriginator() == null || targetArtifact.getOriginator().equals(artifact.getOriginator());
    }

    private boolean matchArtifactType(Artifact artifact, Artifact targetArtifact){
        return targetArtifact.getType() == null || targetArtifact.getType().equals(artifact.getType());
    }

    private boolean matchArtifactValue(Artifact artifact, Artifact targetArtifact){
        return targetArtifact.getValue() == null || targetArtifact.getValue().equals(artifact.getValue());
    }

    private boolean matchArtifacts(List<Artifact> artifacts, Artifact targetArtifact) {
        return targetArtifact == null || artifacts.stream().filter(art -> matchArtifactOriginator(art, targetArtifact) && matchArtifactType(art, targetArtifact) && matchArtifactValue(art, targetArtifact)).count() > 0;
    }
}
