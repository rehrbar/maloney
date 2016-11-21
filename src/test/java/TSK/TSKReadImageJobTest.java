package TSK;

import ch.hsr.maloney.processing.TSKReadImageJob;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.FileExtractor;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
        System.out.printf("java.library.path:" + System.getProperty("java.library.path"));

//        System.load("C:\\projects\\malware-hunting\\lib\\libtsk_jni.dll");
//        System.load("C:\\projects\\malware-hunting\\lib\\zlib.dll");
//        System.load("C:\\projects\\malware-hunting\\lib\\libewf.dll");
//        System.load("C:\\projects\\malware-hunting\\lib\\libvmdk.dll");
//        System.load("C:\\projects\\malware-hunting\\lib\\libvhdi.dll");

        System.loadLibrary("libtsk_jni");
        System.loadLibrary("zlib");
        System.loadLibrary("libewf");
        System.loadLibrary("libvmdk");
        System.loadLibrary("libvhdi");

        TSKReadImageJob tskReadImageJob = new TSKReadImageJob();
        DataSource dataSource = new fakeDataSource();

        tskReadImageJob.run(new Context(null , null, dataSource),
                new Event("newDiskImage","Test", UUID.randomUUID()));
    }
}
