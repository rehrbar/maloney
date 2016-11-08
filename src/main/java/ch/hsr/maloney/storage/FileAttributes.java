package ch.hsr.maloney.storage;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by olive_000 on 07.11.2016.
 */
public class FileAttributes {
    private String FileName;
    private String FIlePath;
    private UUID FileId;
    private Date DateChanged;
    private Date DateCreated;
    private Date DateAccessed;
    private List<Artifact> artifacts;

    public FileAttributes(String fileName, String FIlePath, UUID fileId, Date dateChanged, Date dateCreated, Date dateAccessed, List<Artifact> artifacts) {
        FileName = fileName;
        this.FIlePath = FIlePath;
        FileId = fileId;
        DateChanged = dateChanged;
        DateCreated = dateCreated;
        DateAccessed = dateAccessed;
        this.artifacts = artifacts;
    }

    public String getFileName() {
        return FileName;
    }

    public String getFIlePath() {
        return FIlePath;
    }

    public UUID getFileId() {
        return FileId;
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
