package ch.hsr.maloney.storage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private Path jobsWorkingDirPath;

    public FakeDataSource(){
        try {
            jobsWorkingDirPath = Files.createTempDirectory("maloney-test");
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
        fileExtractor.extractMetadata();
        Path file = fileExtractor.extractFile();
        fileExtractor.cleanup();
        fileUuidToPath.put(id, file);
        return id;
    }

    @Override
    public Path getJobWorkingDir(Class job) {
        return workingDirs.computeIfAbsent(job, aClass -> jobsWorkingDirPath.resolve(job.getSimpleName() + "_" + job.getCanonicalName().hashCode()));
    }

    public UUID addFile(Path path) {
        UUID uuid = UUID.randomUUID();
        fileUuidToPath.put(uuid, path);
        return uuid;
    }

    public Collection<Path> getFiles() {
        return fileUuidToPath.values();
    }

    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(jobsWorkingDirPath.toFile());
    }
}
