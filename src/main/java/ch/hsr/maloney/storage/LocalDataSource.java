package ch.hsr.maloney.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Data source which manages local data on the file system.
 */
public class LocalDataSource implements DataSource {
    MetadataStore metadataStore;
    Path workingDirPath;
    Logger logger = LogManager.getLogger();

    public LocalDataSource(MetadataStore metadataStore) {
        this.metadataStore = metadataStore;
        try {
            workingDirPath = Files.createTempDirectory("maloney");
            logger.debug("Created temporary working directory: {}", workingDirPath.toString());
        } catch (IOException e) {
            logger.error("Could not create temporary working directory.", e);
        }
    }
    @Override
    public void registerFileAttributes() {
        // TODO remove after refactoring???
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFile(UUID fileID) {
        return new File(workingDirPath.resolve(fileID.toString()).toString());
    }

    @Override
    public InputStream getFileStream(UUID fileID) {
        try {
            return Files.newInputStream(workingDirPath.resolve(fileID.toString()), StandardOpenOption.READ);
        } catch (IOException e) {
            logger.error("Could not open file stream.");
        }
        return null;
    }

    @Override
    public UUID addFile(String path, UUID parentId) {
        // TODO remove after refactoring
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID addFile(UUID parentId, FileExtractor fileExtractor) {
        // TODO add overload to suppress file copy.
        UUID uuid = UUID.randomUUID();

        // Extracting necessary information
        FileSystemMetadata metadata = fileExtractor.extractMetadata();
        // TODO check if the file needs to be extracted and added again.

        // We do not gather the file. This should speed up this a little bit.
        Path path = fileExtractor.extractFile();
        try {
            Files.copy(path, workingDirPath.resolve(uuid.toString()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Could not add file to local working director. File: " + path.toString(), e);
        }
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
}
