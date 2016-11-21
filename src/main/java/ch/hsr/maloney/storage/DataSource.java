package ch.hsr.maloney.storage;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by olive_000 on 01.11.2016.
 */
public interface DataSource {
    /**
     * Reads all Files in the Source and adds its MetaData to the MetadataStore
     */
    void registerFileAttributes();

    /**
     * Get a file from the DataSource as a File
     *
     * @param fileID    Unique ID of file to get
     * @return          Specified File as File
     */
    File getFile(UUID fileID);

    /**
     * Get a file from the DataSource as a Stream
     *
     * @param fileID    Unique ID of the file to get
     * @return          Specified File as Stream or null, if the file could not be opened.
     */
    InputStream getFileStream(UUID fileID);

    /**
     * Adds a new File to the DataSource
     *
     * @param path      Path of the file to add to the DataSource
     * @param parentId  UUID of the parent, null if there is none (e.g. root image)
     */
    // TODO replace with new addFile implementation
    UUID addFile(String path, UUID parentId);

    /**
     * Adds a new File to the DataSource
     *  @param parentId  UUID of the parent, null if there is none (e.g. root image)
     * @param fileExtractor Used to exctract the file itself.
     */
    UUID addFile(UUID parentId, FileExtractor fileExtractor);
}
