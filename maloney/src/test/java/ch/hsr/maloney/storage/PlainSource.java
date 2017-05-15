package ch.hsr.maloney.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Data source which manages small data sets. Probably only useful for tests, but nothing more. No real handling of files.
 */
public class PlainSource implements DataSource {
    private Logger logger;
    private MetadataStore metadataStore;

    private Path workingDirPath;
    private Path jobsWorkingDirPath;

    public PlainSource(MetadataStore metadataStore) {
        this.metadataStore = metadataStore;
        this.logger = LogManager.getLogger();

        // Initialize a working directory in the temp file directory
        try {
            workingDirPath = Files.createTempDirectory("maloney");
            logger.debug("Created temporary working directory: {}", workingDirPath.toString());
        } catch (IOException e) {
            logger.error("Could not create temporary working directory.", e);
        }

        // Prepare the working directories.
        jobsWorkingDirPath = workingDirPath.resolve("jobs");
        try {
            Files.createDirectories(jobsWorkingDirPath);
        } catch (IOException e) {
            logger.error("Could not create working directory for Jobs");
        }
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
                uuid, metadata.getDateChanged(),
                metadata.getDateCreated(),
                metadata.getDateAccessed(),
                null,
                parentId));
        return uuid;
    }

    @Override
    public Path getJobWorkingDir(Class job) {
        return jobsWorkingDirPath.resolve(job.getSimpleName() + "_" + job.getCanonicalName().hashCode());
    }

    public void cleanUp(){
        try {
            recursiveDelete(workingDirPath.toFile());
        } catch (IOException e) {
            logger.error("Failed to clean up after test", e);
        }
    }

    private void recursiveDelete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                recursiveDelete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to recursiveDelete file: " + f);
        }
    }
}
