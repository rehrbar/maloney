package ch.hsr.maloney.storage;

import java.util.*;

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

    public FileAttributes() {
        // Keep for deserialization.
    }

    public FileAttributes(String fileName, String filePath, UUID fileId, Date dateChanged, Date dateCreated, Date dateAccessed, UUID parentId) {
        this(fileName, filePath, fileId, dateChanged, dateCreated, dateAccessed, parentId, null);
    }

    public FileAttributes(String fileName, String filePath, UUID fileId, Date dateChanged, Date dateCreated, Date dateAccessed, UUID parentId, Collection<Artifact> artifacts) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileId = fileId;
        this.parentId = parentId;
        this.dateChanged = dateChanged;
        this.dateCreated = dateCreated;
        this.dateAccessed = dateAccessed;
        if (artifacts != null) {
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

    public Collection<Artifact> getArtifacts() {
        return artifacts;
    }

    public UUID getParentId() {
        return parentId;
    }
}
