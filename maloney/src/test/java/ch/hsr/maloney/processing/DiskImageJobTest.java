package ch.hsr.maloney.processing;

import ch.hsr.maloney.core.FrameworkEventNames;
import ch.hsr.maloney.storage.*;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FakeMetaDataStore;
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
    private FakeMetaDataStore fakeMetaDataStore;
    private FakeDataSource fakeDataSource;
    private Context ctx;

    private class FakeDataSource implements DataSource {

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
        fakeMetaDataStore = new FakeMetaDataStore();
        fakeDataSource = new FakeDataSource();
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
