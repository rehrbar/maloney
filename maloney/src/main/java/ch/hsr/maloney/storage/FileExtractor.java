package ch.hsr.maloney.storage;

import java.nio.file.Path;

/**
 * The FileExtractor interface should be implemented by every class which can extract a file.
 * How the file is extracted is up to the implementation.
 */
public interface FileExtractor {
    /**
     * Tells whether the file should be copied into the datastore or if the original should be referenced and used.
     *
     * @return True, if the original should be used.
     */
    boolean useOriginalFile();

    /**
     * Extracts a file and passes the path to this extracted file to the caller.
     *
     * @return Path to extracted file.
     */
    Path extractFile();

    /**
     * Extracts the meta data of a file.
     *
     * @return Extracted metadata.
     */
    FileSystemMetadata extractMetadata();

    /**
     * Will be called after file extraction to cleanup temporary files.
     */
    void cleanup();
}
