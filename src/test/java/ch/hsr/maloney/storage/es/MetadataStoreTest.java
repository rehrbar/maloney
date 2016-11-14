package ch.hsr.maloney.storage.es;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by roman on 13.11.16.
 */
public class MetadataStoreTest {

    private MetadataStoreTestImpl es;
    private List<String> generatedIds;


    @Before
    public void setUp() throws Exception {
        es = new MetadataStoreTestImpl();
        es.clearIndex();
        generatedIds = es.seedTestData();
        es.refreshIndex();
    }

    @Test
    public void indexFileAttributesTest(){
        FileAttributes attributes = new FileAttributes(
                "regedit.exe",
                "C:\\Windows\\",
                UUID.fromString("d5c7bdcb-9286-48d9-bd13-e6bbe1e81652"),
                new Date(1436471820000L),
                new Date(1473823035000L),
                new Date(1473823035000L),
                new LinkedList<Artifact>(){{
                    add(new Artifact("test","SGVsbG8gd29ybGQh","base64"));
                    add(new Artifact("test","86fb269d190d2c85f6e0468ceca42a20","md5"));
                }});
        es.addFileAttributes(attributes);
    }

    @Test
    public void getFileAttributesTest(){
        FileAttributes attributes = es.getFileAttributes(UUID.fromString("f99f4262-7b84-440a-b650-ccdd30940511"));
        Assert.assertEquals("cmd.exe", attributes.getFileName());
        Assert.assertEquals("C:\\Windows\\", attributes.getFilePath());
        Assert.assertEquals("da37863e-aa7d-11e6-80f5-76304dec7eb7", attributes.getFileId().toString());
    }

    @Test
    public void addSingleArtifactTest(){
        Artifact art = new Artifact("test","SGVsbG8gd29ybGQh","base64");
        UUID fileId = UUID.fromString("f99f4262-7b84-440a-b650-ccdd30940511");
        es.addArtifact(fileId, art);
        String result = es.dumpFileAttributeSource(fileId);
        Assert.assertTrue(result.contains("SGVsbG8gd29ybGQh"));
    }

    @Test
    public void addMultipleArtifactTest(){
        List<Artifact> artifacts = new LinkedList<Artifact>(){{
            push( new Artifact("test","SGVsbG8gd29ybGQh","base64"));
            push(new Artifact("test","86fb269d190d2c85f6e0468ceca42a20","md5"));
        }};
        UUID fileId = UUID.fromString("f99f4262-7b84-440a-b650-ccdd30940511");
        es.addArtifacts(fileId, artifacts);
        String result = es.dumpFileAttributeSource(fileId);
        Assert.assertTrue(result.contains("base64"));
        Assert.assertTrue(result.contains("SGVsbG8gd29ybGQh"));
        Assert.assertTrue(result.contains("md5"));
        Assert.assertTrue(result.contains("86fb269d190d2c85f6e0468ceca42a20"));
    }
}

