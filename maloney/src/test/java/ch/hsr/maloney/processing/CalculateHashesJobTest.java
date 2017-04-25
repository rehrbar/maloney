package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.*;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FakeMetaDataStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalculateHashesJobTest {
    private Path tempFilePath;
    private UUID tempFileUuid;
    private FakeMetaDataStore fakeMetaDataStore;
    private fakeDataSource fakeDataSource;
    private Context ctx;
    public static final String ZERO_LENGTH_SHA_1_HASH = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
    public static final String ZERO_LENGTH_MD5_HASH = "d41d8cd98f00b204e9800998ecf8427e";

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
            throw new UnsupportedOperationException();
        }

        @Override
        public Path getJobWorkingDir(Class job) {
            throw new UnsupportedOperationException();
        }

        public UUID addFile(Path path) {
            UUID uuid = UUID.randomUUID();
            fileUuidToPath.put(uuid, path);
            return uuid;
        }

    }

    @Before
    public void prepare() {
        try {
            tempFilePath = Files.createTempFile("CHJT-testfile-", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fakeMetaDataStore = new FakeMetaDataStore();
        fakeDataSource = new fakeDataSource();
        ctx = new Context(fakeMetaDataStore, null, fakeDataSource);
        tempFileUuid = fakeDataSource.addFile(tempFilePath);
    }

    @After
    public void cleanUp() {
        fakeDataSource.fileUuidToPath.forEach((uuid, path) -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void canRunTest() {
        Job job = new CalculateHashesJob();

        List<String> reqEvent = job.getRequiredEvents();
        Event evt = new Event(reqEvent.get(reqEvent.size() - 1), "Test", UUID.randomUUID());

        assertTrue(job.canRun(ctx, evt));
    }

    @Test
    public void singleFileTest() {
        Job job = new CalculateHashesJob();
        Event evt = new Event("newFile", "singleFileTest", tempFileUuid);

        job.run(ctx, evt);

        List<Artifact> createdArtifacts = fakeMetaDataStore.getArtifacts(tempFileUuid);
        assertTrue(createdArtifacts.size() == 2);
    }

    @Test
    public void checkMD5Hash() {
        Job job = new CalculateHashesJob();
        Event evt = new Event("newFile", "singleFileTest", tempFileUuid);

        job.run(ctx, evt);

        List<Artifact> createdArtifacts = fakeMetaDataStore.getArtifacts(tempFileUuid);
        createdArtifacts.stream().filter(a -> a.getType().equals("MD5Hash")).forEach(a -> {
            assertEquals(ZERO_LENGTH_MD5_HASH, a.getValue().toString());
        });
    }

    @Test
    public void checkSHA1Hash() {
        Job job = new CalculateHashesJob();
        Event evt = new Event("newFile", "singleFileTest", tempFileUuid);

        job.run(ctx, evt);

        List<Artifact> createdArtifacts = fakeMetaDataStore.getArtifacts(tempFileUuid);
        createdArtifacts.stream().filter(a -> a.getType().equals("SHA1Hash")).forEach(a -> {
            assertEquals(ZERO_LENGTH_SHA_1_HASH, a.getValue().toString());
        });
    }

    @Test
    public void checkFileWithContentHash() throws IOException {
        FileOutputStream fos = new FileOutputStream(tempFilePath.toFile(),false);
        fos.write("Hello world!".getBytes());
        fos.close();

        Job job = new CalculateHashesJob();
        Event evt = new Event("newFile", "singleFileTest", tempFileUuid);

        job.run(ctx, evt);

        List<Artifact> createdArtifacts = fakeMetaDataStore.getArtifacts(tempFileUuid);
        createdArtifacts.stream().filter(a -> a.getType().equals("SHA1Hash")).forEach(a -> {
            assertEquals("d3486ae9136e7856bc42212385ea797094475802", a.getValue().toString());
        });
        createdArtifacts.stream().filter(a -> a.getType().equals("MD5Hash")).forEach(a -> {
            assertEquals("86fb269d190d2c85f6e0468ceca42a20", a.getValue().toString());
        });
    }
}
