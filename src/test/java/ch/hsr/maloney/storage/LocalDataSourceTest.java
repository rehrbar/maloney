package ch.hsr.maloney.storage;

import org.junit.Assert;
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

    public LocalDataSourceTest(){
        metadataStore = new SimpleMetadataStore();
        dataSource = new LocalDataSource(metadataStore);
    }

    @Test
    public void addFileTest() throws IOException {
        Path tempFile = Files.createTempFile("maloney","");
        UUID uuid = dataSource.addFile(null, new FileExtractor() {

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
        });

        FileAttributes fileAttributes = metadataStore.getFileAttributes(uuid);
        Assert.assertEquals("notepad.exe", fileAttributes.getFileName());

        File file = dataSource.getFile(uuid);
        Assert.assertTrue(file.exists());
    }
}
