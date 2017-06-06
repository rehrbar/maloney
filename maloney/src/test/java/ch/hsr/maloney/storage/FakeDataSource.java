package ch.hsr.maloney.storage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by roman on 11.04.17.
 */
public class FakeDataSource implements DataSource {

    Map<UUID, Path> fileUuidToPath = new HashMap<>();
    Map<Class, Path> workingDirs = new HashMap<>();
    private MetadataStore metadataStore;
    private Path jobsWorkingDirPath;
    private Path filesWorkingDirPath;

    public FakeDataSource(MetadataStore metadataStore){
        this.metadataStore = metadataStore;
        try {
            jobsWorkingDirPath = Files.createTempDirectory("maloney-test");
            filesWorkingDirPath = jobsWorkingDirPath.resolve("files");
            Files.createDirectories(filesWorkingDirPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getFile(UUID fileID) {
        return fileUuidToPath.get(fileID).toFile();
    }

    @Override
    public FileInputStream getFileStream(UUID fileID) {
        try {
            return new FileInputStream(fileUuidToPath.get(fileID).toFile());
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public UUID addFile(UUID parentId, FileExtractor fileExtractor) {
        UUID id = UUID.randomUUID();
        FileSystemMetadata fileSystemMetadata = fileExtractor.extractMetadata();
        metadataStore.addFileAttributes(new FileAttributes(
                fileSystemMetadata.getFileName(),
                fileSystemMetadata.getFilePath(),
                id,
                fileSystemMetadata.getDateChanged(),
                fileSystemMetadata.getDateCreated(),
                fileSystemMetadata.getDateAccessed(),
                parentId
        ));

        Path path = fileExtractor.extractFile();
        if (!fileExtractor.useOriginalFile()) {
            try {
                path = Files.copy(path, filesWorkingDirPath.resolve(id.toString()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileUuidToPath.put(id, path);
        fileExtractor.cleanup();
        return id;
    }

    @Override
    public Path getJobWorkingDir(Class job) {
        return workingDirs.computeIfAbsent(job, aClass -> jobsWorkingDirPath.resolve(job.getSimpleName() + "_" + job.getCanonicalName().hashCode()));
    }

    /**
     * Adds a file to the store. Just for testing purposes.
     * @param path Path to file to add.
     * @param fileAttributes
     * @return Generated ID of the added file.
     */
    public UUID addFile(Path path, FileAttributes fileAttributes) {
        UUID uuid = UUID.randomUUID();
        fileUuidToPath.put(uuid, path);
        if(fileAttributes != null){
            metadataStore.addFileAttributes(fileAttributes);
        } else {
            metadataStore.addFileAttributes(new FileAttributes(
                    path.getFileName().toString(),
                    path.getParent().toString(),
                    uuid,
                    null, null, null, null
            ));
        }
        return uuid;
    }

    public Collection<Path> getFiles() {
        return fileUuidToPath.values();
    }

    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(jobsWorkingDirPath.toFile());
    }
}
