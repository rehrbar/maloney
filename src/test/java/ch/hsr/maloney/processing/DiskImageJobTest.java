package ch.hsr.maloney.processing;

import ch.hsr.maloney.core.FrameworkEventNames;
import ch.hsr.maloney.storage.*;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static ch.hsr.maloney.core.Framework.EVENT_ORIGIN;

/**
 * Created by roman on 03.12.16.
 */
public class DiskImageJobTest {
    private fakeMetaDataStore fakeMetaDataStore;
    private fakeDataSource fakeDataSource;
    private Context ctx;

    private class fakeMetaDataStore implements MetadataStore {

        Map<UUID, List<Artifact>> artifacts = new HashMap<>();

        @Override
        public FileAttributes getFileAttributes(UUID fileID) {
            return null;
        }

        @Override
        public void addFileAttributes(FileAttributes fileAttributes) {

        }

        @Override
        public List<Artifact> getArtifacts(UUID fileId) {
            return artifacts.get(fileId);
        }

        @Override
        public void addArtifact(UUID fileId, Artifact artifact) {
            List<Artifact> artifactList = artifacts.get(fileId);
            if (artifactList == null) {
                artifactList = new LinkedList<>();
            }
            artifactList.add(artifact);
            artifacts.put(fileId, artifactList);
        }

        @Override
        public void addArtifacts(UUID fileId, List<Artifact> artifacts) {
            for (Artifact a : artifacts) {
                addArtifact(fileId, a);
            }
        }

    }

    private class fakeDataSource implements DataSource {

        Map<UUID, Path> fileUuidToPath = new HashMap<>();

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
            throw new UnsupportedOperationException();
        }

        public Collection<Path> getFiles(){
            return fileUuidToPath.values();
        }

    }
    private Path testImage;
    @Before
    public void setup(){
        fakeMetaDataStore = new fakeMetaDataStore();
        fakeDataSource = new fakeDataSource();
        ctx = new Context(fakeMetaDataStore, null, fakeDataSource);
    }

    @After
    public void cleanup() throws IOException {
        Files.deleteIfExists(testImage);
    }

    @Test
    public void run() throws IOException {
        Event startupEvent = new Event(FrameworkEventNames.STARTUP, EVENT_ORIGIN, null);
        testImage = Files.createTempFile("maloney", null);
        Job job = new DiskImageJob();
        job.setJobConfig(testImage.toString());
        Assert.assertTrue(job.canRun(ctx, startupEvent));
        job.run(ctx, startupEvent);
        Assert.assertTrue(fakeDataSource.getFiles().contains(testImage));
    }
}
