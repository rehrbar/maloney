package ch.hsr.maloney.storage;

import java.util.Date;

/**
 * Contains the metadata for a file, which is usually stored on a file system.
 */
public class FileSystemMetadata {
    private String fileName;
    private String filePath;
    private Date dateChanged;
    private Date dateCreated;
    private Date dateAccessed;
    private long size;

    /**
     * Creates a new instance of FileSystemMetadata.
     */
    public FileSystemMetadata(){

    }

    /**
     * Creates a new instance of FileSystemMetadata.
     * @param fileName Name of the file.
     * @param filePath Path of the file, excluding the actual file name.
     * @param dateCreated Date when the file was created.
     * @param dateChanged Date when the file was changed.
     * @param dateAccessed Date when the file was last accessed.
     * @param size Size in bytes.
     */
    public FileSystemMetadata(String fileName, String filePath, Date dateCreated, Date dateChanged, Date dateAccessed, long size){
        this.fileName = fileName;
        this.filePath = filePath;
        this.dateCreated = dateCreated;
        this.dateChanged = dateChanged;
        this.dateAccessed = dateAccessed;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateAccessed() {
        return dateAccessed;
    }

    public void setDateAccessed(Date dateAccessed) {
        this.dateAccessed = dateAccessed;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
