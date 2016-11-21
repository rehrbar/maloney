package TSK;

import ch.hsr.maloney.processing.TSKReadImageJob;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.FileExtractor;
import ch.hsr.maloney.storage.FileSystemMetadata;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class TSKReadImageJobTest {
    private static Path workingDir;
    private static Logger logger;

    private class fakeDataSource implements DataSource{
        private final Map<UUID, FileExtractor> savedFiles;

        public fakeDataSource() {
            this.savedFiles = new HashMap<>();
        }


        @Override
        public void registerFileAttributes() {
            // ... soon to be deprecated ...
        }

        @Override
        public File getFile(UUID fileID) {
            return new File(savedFiles.get(fileID).extractFile().toString());
        }

        @Override
        public InputStream getFileStream(UUID fileID) {
            try {
                return new FileInputStream(getFile(fileID));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public UUID addFile(String path, UUID parentId) {
            // ... soon to be deprecated ...
            return null;
        }

        @Override
        public UUID addFile(UUID parentId, FileExtractor fileExtractor) {
            UUID uuid = UUID.randomUUID();
            savedFiles.put(uuid, fileExtractor);
            return uuid;
        }

        @Override
        public Path getJobWorkingDir(Class job) {
            return workingDir;
        }

        Map<UUID, FileExtractor> getSavedFiles() {
            return savedFiles;
        }
    }

    @BeforeClass
    public static void prepare(){
        logger = LogManager.getLogger();
        try {
            workingDir = Files.createTempDirectory("Maloney-");
        } catch (IOException e) {
            logger.error("Could not create temp directory at: {}",workingDir,e);
        }
    }

    @AfterClass
    public static void cleanUp(){
        try {
            Files.deleteIfExists(workingDir);
        } catch (IOException e) {
            logger.error("Could not delete temp directory at: {}",workingDir,e);
        }
    }

    @Test
    public void simpleTest(){
        System.loadLibrary("libtsk_jni");
        System.loadLibrary("zlib");
        System.loadLibrary("libewf");
        System.loadLibrary("libvmdk");
        System.loadLibrary("libvhdi");

        TSKReadImageJob tskReadImageJob = new TSKReadImageJob();
        fakeDataSource dataSource = new fakeDataSource();

        UUID uuid = dataSource.addFile(null, new FileExtractor() {
            @Override
            public Path extractFile() {
                //TODO insert position of image
                return Paths.get("C:\\projects\\malware-hunting\\images\\autopsy-demo-disk.dd");
            }

            @Override
            public FileSystemMetadata extractMetadata() {
                return null;
            }

            @Override
            public void cleanup() {

            }
        });

        tskReadImageJob.run(new Context(null , null, dataSource),
                new Event("newDiskImage","Test", uuid));

        Assert.assertTrue(dataSource.getSavedFiles().size() > 1);
    }
}
