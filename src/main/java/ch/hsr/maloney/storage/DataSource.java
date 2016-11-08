package ch.hsr.maloney.storage;

import java.util.UUID;

/**
 * Created by olive_000 on 01.11.2016.
 */
public interface DataSource<T> {
    /**
     * Reads all Files in the Source and adds its MetaData to the MetadataStore
     */
    void registerFileAttributes();

    /**
     * Returns file in format T
     *
     * @param fileID    Unique ID of file to get
     * @return          Specified File
     */
    T getFile(UUID fileID);

    /**
     * Adds a new File to the DataSource
     *
     * @param path      Path of the file to add to the DataSource
     */
    UUID addFile(String path);
}
