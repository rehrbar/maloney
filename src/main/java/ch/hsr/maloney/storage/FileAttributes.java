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
    private UUID parentId;
    private Date dateChanged;
    private Date dateCreated;
    private Date dateAccessed;
    private List<Artifact> artifacts = new LinkedList<>();

    public FileAttributes(){
        // Keep for deserialization.
    }

    public FileAttributes(String fileName, String filePath, UUID fileId, Date dateChanged, Date dateCreated, Date dateAccessed, List<Artifact> artifacts, UUID parentId) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileId = fileId;
        this.parentId = parentId;
        this.dateChanged = dateChanged;
        this.dateCreated = dateCreated;
        this.dateAccessed = dateAccessed;
        if(artifacts ==  null){
            this.artifacts.addAll(artifacts);
        }
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
        return dateChanged;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateAccessed() {
        return dateAccessed;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }
}
