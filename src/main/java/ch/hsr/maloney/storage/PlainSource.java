package ch.hsr.maloney.storage;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Data source which manages small data sets. Probably only useful for tests, but nothing more. No real handling of files.
 */
public class PlainSource implements DataSource {
    MetadataStore metadataStore;

    public PlainSource(MetadataStore metadataStore) {
        this.metadataStore = metadataStore;
    }

    @Override
    public File getFile(UUID fileID) {
        return new File(metadataStore.getFileAttributes(fileID).getFilePath());
    }

    @Override
    public InputStream getFileStream(UUID fileID) {
        //TODO get as FileStream
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID addFile(UUID parentId, FileExtractor fileExtractor) {
        UUID uuid = UUID.randomUUID();

        // Extracting necessary information
        FileSystemMetadata metadata = fileExtractor.extractMetadata();
        // We do not gather the file. This should speed up this a little bit.
        fileExtractor.cleanup();

        // Updating MetadataStore with new information.
        metadataStore.addFileAttributes(new FileAttributes(
                metadata.getFileName(),
                metadata.getFilePath(),
                uuid,metadata.getDateChanged(),
                metadata.getDateCreated(),
                metadata.getDateAccessed(),
                null,
                parentId));
        return uuid;
    }

    @Override
    public Path getJobWorkingDir(Class job) {
        return null;
    }
}
