package ch.hsr.maloney.storage;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class LocalDataSourceTest {
    private LocalDataSource dataSource ;
    private MetadataStore metadataStore;

    public LocalDataSourceTest(){
        metadataStore = new SimpleMetadataStore();
        dataSource = new LocalDataSource(metadataStore);
    }

    @Test
    public void addFileTest() throws IOException {
        final Path[] tempFile = {null};
        dataSource.addFile(null, () -> new FileSystemMetadata("notepad.exe", "C:\\windows\\", new Date(1436471820000L), new Date(1436471820000L), new Date(1436471820000L), 1337000L), () -> {
            try {
                tempFile[0] = Files.createTempFile("maloney","");
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("Could not create temp file.");
            }
            return tempFile[0];
        });
        Files.deleteIfExists(tempFile[0]);
    }
}
