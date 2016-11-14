package ch.hsr.maloney.storage;

import java.io.File;
import java.io.FileInputStream;
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
     * @return          Specified File as Stream
     */
    FileInputStream getFileStream(UUID fileID);

    /**
     * Adds a new File to the DataSource
     *
     * @param path      Path of the file to add to the DataSource
     */
    UUID addFile(String path);
}
