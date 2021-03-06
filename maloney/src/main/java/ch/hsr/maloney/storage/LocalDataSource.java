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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Data source which manages local data on the file system.
 */
public class LocalDataSource implements DataSource {
    MetadataStore metadataStore;
    Path workingDirPath;
    Logger logger = LogManager.getLogger();
    private Path jobsWorkingDirPath;
    private Path filesWorkingDirPath;
    private Map<UUID, Path> fileReferences = new HashMap<>();

    /**
     * Creates an instance of a local data source.
     *
     * @param metadataStore    Used to store additional meta information.
     * @param workingDirectory Path to a directory which should be used as working directory. If null is provided, the
     *                         temporary directory configured is used.
     */
    public LocalDataSource(MetadataStore metadataStore, Path workingDirectory) {
        this.metadataStore = metadataStore;
        if (workingDirectory == null) {
            throw new IllegalArgumentException("No working directory provided");
        }
        workingDirPath = workingDirectory;

        // Prepare the working directories.
        jobsWorkingDirPath = workingDirPath.resolve("jobs");
        filesWorkingDirPath = workingDirPath.resolve("files");
        try {
            Files.createDirectories(jobsWorkingDirPath);
            Files.createDirectories(filesWorkingDirPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getFile(UUID fileID) {
        return new File(getFilePath(fileID).toString());
    }

    @Override
    public InputStream getFileStream(UUID fileID) {
        try {
            return Files.newInputStream(getFilePath(fileID), StandardOpenOption.READ);
        } catch (IOException e) {
            logger.error("Could not open file stream.");
        }
        return null;
    }

    private Path getFilePath(UUID fileID) {
        if (fileReferences.containsKey(fileID)) {
            return fileReferences.get(fileID);
        }
        return filesWorkingDirPath.resolve(fileID.toString());
    }

    @Override
    public UUID addFile(UUID parentId, FileExtractor fileExtractor) {
        UUID uuid = UUID.randomUUID();

        // Extracting necessary information
        FileSystemMetadata metadata = fileExtractor.extractMetadata();

        // TODO check if the file needs to be extracted and added again.
        // We should not gather the file if it's not required. This should speed up the process a little bit.
        Path path = fileExtractor.extractFile();

        if (fileExtractor.useOriginalFile()) {
            fileReferences.put(uuid, path);
        } else {
            try {
                Files.copy(path, filesWorkingDirPath.resolve(uuid.toString()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("Could not add file to local working director. File: " + path.toString(), e);
            }
        }
        fileExtractor.cleanup();

        // Updating MetadataStore with new information.
        metadataStore.addFileAttributes(new FileAttributes(
                metadata.getFileName(),
                metadata.getFilePath(),
                uuid, metadata.getDateChanged(),
                metadata.getDateCreated(),
                metadata.getDateAccessed(),
                parentId));
        return uuid;
    }

    @Override
    public Path getJobWorkingDir(Class job) {
        // Providing the simple name for an power user to find temporary files in the working dir.
        // Adding hashed canonical name to supply an unique identifier with a shorter length.
        // If an absolute identifier is required, use only CN instead.
        return jobsWorkingDirPath.resolve(job.getSimpleName() + "_" + job.getCanonicalName().hashCode());
    }

}
