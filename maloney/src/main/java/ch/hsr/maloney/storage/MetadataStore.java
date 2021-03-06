package ch.hsr.maloney.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by olive_000 on 07.11.2016.
 */
public interface MetadataStore {
    /**
     * Get the File Atrributes of a specified File
     *
     * @param fileID Id of the file
     * @return File Attributes of the specified file
     */
    FileAttributes getFileAttributes(UUID fileID);

    /**
     * Adds file attributes to the {@link MetadataStore }.
     *
     * @param fileAttributes File attributes to add.
     */
    void addFileAttributes(FileAttributes fileAttributes);

    /**
     * Gets all the artifacts for a specific file.
     *
     * @param fileId Id of the file
     * @return Artifacts which are tethered to the specified file
     */
    Collection<Artifact> getArtifacts(UUID fileId);

    /**
     * Adds only the artifacts to an already existing file.
     *
     * @param fileId   Id of the file
     * @param artifact Tethers this artifact to file
     */
    void addArtifact(UUID fileId, Artifact artifact);

    /**
     * Adds multiple artifacts for a single file.
     *
     * @param fileId    Id of the file.
     * @param artifacts Artifacts to be added.
     */
    void addArtifacts(UUID fileId, Collection<Artifact> artifacts);

    /**
     * Get an Iterator over all FileAttributes stored in the MetadataStore.
     *
     * @return  Iterator over all FileAttributes in MetadataStore.
     */
    Iterator<FileAttributes> iterator();
}
