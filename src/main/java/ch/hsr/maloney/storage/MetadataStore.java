package ch.hsr.maloney.storage;

import java.util.List;

/**
 * Created by olive_000 on 07.11.2016.
 */
public interface MetadataStore {
    /**
     * Get the File Atrributes of a specified File
     *
     * @param fileID    Id of the file
     * @return          File Attributes of the specified file
     */
    FileAttributes getFileAttributes(String fileID);

    /**
     *
     * @param fileAttributes    Add these File Attributes to the MetadataStore
     */
    void addFileAttributes(FileAttributes fileAttributes);

    /**
     *
     * @param fileId    Id of the file
     * @return          Artifacts which are tethered to the specified file
     */
    List<Artifact> getArtifacts(String fileId);

    /**
     *
     * @param fileId     Id of the file
     * @param artifact   Tethers this artifact to file
     */
    void addArtifact(String fileId, Artifact artifact);
}
