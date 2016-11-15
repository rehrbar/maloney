package ch.hsr.maloney.storage;

/**
 * The MetadataExtractor interface should be implemented by every class which can extract metadata of a file.
 * How the FileSystemMetadata is extracted is up to the implementation.
 */
public interface MetadataExtractor {
    /**
     * Extracts the meta data of a file.
     * @return Extracted metadata.
     */
    FileSystemMetadata run();
}
