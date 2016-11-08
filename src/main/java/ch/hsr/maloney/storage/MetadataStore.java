package ch.hsr.maloney.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    FileAttributes getFileAttributes(UUID fileID);

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
    List<Artifact> getArtifacts(UUID fileId);

    /**
     *
     * @param fileId     Id of the file
     * @param artifact   Tethers this artifact to file
     */
    void addArtifact(UUID fileId, Artifact artifact);

    /**
     *
     * @param uuidArtifactMap   Map with the Artifacts which should be added
     */
    void addArtifacts(Map<UUID, Artifact> uuidArtifactMap);
}
