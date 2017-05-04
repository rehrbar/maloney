package ch.hsr.maloney.storage;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

public class LocalDataSourceTest {
    private LocalDataSource dataSource ;
    private MetadataStore metadataStore;

    @Before
    public void setup() throws IOException {
        metadataStore = new SimpleMetadataStore();
        Path workingDirectory = Files.createTempDirectory("maloney_test");
        FileUtils.forceDeleteOnExit(workingDirectory.toFile());
        dataSource = new LocalDataSource(metadataStore, workingDirectory);
    }

    @Test
    public void addFileTest() throws IOException {
        Path tempFile = Files.createTempFile("maloney","");
        UUID uuid = dataSource.addFile(null, new MyFileExtractor(tempFile));

        FileAttributes fileAttributes = metadataStore.getFileAttributes(uuid);
        Assert.assertEquals("notepad.exe", fileAttributes.getFileName());

        File file = dataSource.getFile(uuid);
        Assert.assertTrue(file.exists());
    }

    @Test
    public void addFileWithoutCopyTest() throws IOException {
        Path tempFile = Files.createTempFile("maloney","");
        UUID uuid = dataSource.addFile(null, new MyFileExtractor(tempFile){
            @Override
            public boolean useOriginalFile() {
                return true;
            }
        });

        FileAttributes fileAttributes = metadataStore.getFileAttributes(uuid);
        Assert.assertEquals("notepad.exe", fileAttributes.getFileName());

        File file = dataSource.getFile(uuid);
        Assert.assertEquals(tempFile, file.toPath());
    }

    @Test
    public void jobWorkingDirTest(){
        Path jobWorkingDir = dataSource.getJobWorkingDir(LocalDataSourceTest.class);
        System.out.println("Job working dir: " + jobWorkingDir.toString());

        // Following asserts are not really carved in stone.
        Assert.assertTrue(jobWorkingDir.toString().contains("jobs"));
        Assert.assertTrue(jobWorkingDir.toString().contains(LocalDataSourceTest.class.getSimpleName()));
    }

    private class MyFileExtractor implements FileExtractor {

        private final Path tempFile;

        public MyFileExtractor(Path tempFile) {
            this.tempFile = tempFile;
        }

        @Override
        public boolean useOriginalFile() {
            return false;
        }

        @Override
        public Path extractFile() {
            return tempFile;
        }

        @Override
        public FileSystemMetadata extractMetadata() {
            return new FileSystemMetadata(
                    "notepad.exe",
                    "C:\\windows\\",
                    new Date(1436471820000L),
                    new Date(1436471820000L),
                    new Date(1436471820000L),
                    1337000L);
        }

        @Override
        public void cleanup() {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                System.out.println("Could not delete temp file:");
                e.printStackTrace();
            }
        }
    }
}
