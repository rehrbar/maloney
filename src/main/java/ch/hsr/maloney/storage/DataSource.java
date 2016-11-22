package ch.hsr.maloney.storage;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Created by olive_000 on 01.11.2016.
 */
public interface DataSource {

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

    /**
     * Adds a new File to the DataSource
     *  @param parentId  UUID of the parent, null if there is none (e.g. root image)
     * @param fileExtractor Used to exctract the file itself.
     */
    UUID addFile(UUID parentId, FileExtractor fileExtractor);

    /**
     * Get Path to a Directory which can be used as temporary file storage for a Job
     * @param job   Get Directory for the specified Job
     * @return      Path to temporary working directory for the specified Job
     */
    Path getJobWorkingDir(Class job);
}
