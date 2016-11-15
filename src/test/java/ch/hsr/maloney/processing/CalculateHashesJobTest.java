package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by olive_000 on 15.11.2016.
 */
public class CalculateHashesJobTest {
    private static Path tempFilePath;

    private UUID tempFileUuid;
    private fakeMetaDataStore fakeMetaDataStore;
    private fakeDataSource fakeDataSource;
    private Context ctx;

    public CalculateHashesJobTest(){
        try {
            tempFilePath = Files.createTempFile("CHJT-testfile-",null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fakeMetaDataStore = new fakeMetaDataStore();
        fakeDataSource = new fakeDataSource();
        ctx = new Context(fakeMetaDataStore, null, fakeDataSource);
        tempFileUuid = fakeDataSource.addFile(tempFilePath.toString(), null);

    }

    private class fakeMetaDataStore implements MetadataStore{

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
            if(artifactList == null){
                artifactList = new LinkedList<>();
            }
            artifactList.add(artifact);
            artifacts.put(fileId, artifactList);
        }

        @Override
        public void addArtifacts(UUID fileId, List<Artifact> artifacts) {
            for (Artifact a:artifacts) {
                addArtifact(fileId, a);
            }
        }

        public void clean(){
            artifacts = new HashMap<>();
        }
    }

    private class fakeDataSource implements DataSource{

        Map<UUID, String> fileUuidToPath = new HashMap<>();

        @Override
        public void registerFileAttributes() {

        }

        @Override
        public File getFile(UUID fileID) {
            return new File(fileUuidToPath.get(fileID));
        }

        @Override
        public FileInputStream getFileStream(UUID fileID) {
            try {
                return new FileInputStream(new File(fileUuidToPath.get(fileID)));
            } catch (FileNotFoundException e) {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public UUID addFile(String path, UUID parentId) {
            UUID uuid = UUID.randomUUID();
            fileUuidToPath.put(uuid, path);
            return uuid;
        }

        public void clean(){
            fileUuidToPath = new HashMap<>();
        }
    }

    @AfterClass
    public static void cleanUp(){
        try {
            Files.deleteIfExists(tempFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void canRunTest(){
        Job job = new CalculateHashesJob();

        List<String> reqEvent = job.getRequiredEvents();
        Event evt = new Event(reqEvent.get(reqEvent.size()-1), "Test", UUID.randomUUID());

        assertTrue(job.canRun(ctx, evt));
    }

    @Test
    public void singleFileTest(){
        Job job = new CalculateHashesJob();
        Event evt = new Event("newFile","singleFileTest", tempFileUuid);

        job.run(ctx, evt);

        List<Artifact> createdArtifacts = fakeMetaDataStore.getArtifacts(tempFileUuid);
        assertTrue(createdArtifacts.size() == 2);
    }
/*
    @Test
    public void checkMD5Hash(){
        final String zeroLengthMD5Hash = "d41d8cd98f00b204e9800998ecf8427e";

        Job job = new CalculateHashesJob();
        Event evt = new Event("newFile","singleFileTest", tempFileUuid);

        job.run(ctx, evt);

        List<Artifact> createdArtifacts = fakeMetaDataStore.getArtifacts(tempFileUuid);
        createdArtifacts.stream().filter(a -> a.getType().equals("MD5Hash")).forEach(a -> {
            assertEquals(zeroLengthMD5Hash, a.getValue());
        });
    }

    @Test
    public void checkSHA1Hash(){
        final String zeroLengthSHA1Hash = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

        Event evt;

        Job job = new CalculateHashesJob();
        evt = new Event("newFile","singleFileTest", tempFileUuid);

        job.run(ctx, evt);

        List<Artifact> createdArtifacts = fakeMetaDataStore.getArtifacts(tempFileUuid);
        createdArtifacts.stream().filter(a -> a.getType().equals("SHA1Hash")).forEach(a -> {
            assertEquals(zeroLengthSHA1Hash, a.getValue());
        });
    }
*/
}
