package ch.hsr.maloney.storage;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by olive_000 on 07.11.2016.
 */
public class FileAttributes {
    private String fileName;
    private String filePath;
    private UUID fileId;
    private Date DateChanged;
    private Date DateCreated;
    private Date DateAccessed;
    private List<Artifact> artifacts;

    public FileAttributes(String fileName, String filePath, UUID fileId, Date dateChanged, Date dateCreated, Date dateAccessed, List<Artifact> artifacts) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileId = fileId;
        DateChanged = dateChanged;
        DateCreated = dateCreated;
        DateAccessed = dateAccessed;
        this.artifacts = new LinkedList<>(artifacts);
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public UUID getFileId() {
        return fileId;
    }

    public Date getDateChanged() {
        return DateChanged;
    }

    public Date getDateCreated() {
        return DateCreated;
    }

    public Date getDateAccessed() {
        return DateAccessed;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void addArtifact(Artifact artifact){
        artifacts.add(artifact);
    }

    public void removeArtifact(Artifact artifact){
        artifacts.remove(artifact);
    }
}
