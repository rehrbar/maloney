package ch.hsr.maloney.storage;

import java.nio.file.Path;

/**
 * The FileExtractor interface should be implemented by every class which can extract a file.
 * How the file is extracted is up to the implementation.
 */
public interface FileExtractor {
    /**
     * Extracts a file and passes the path to this extracted file to the caller.
     * @return Path to extracted file.
     */
    Path run();
}
