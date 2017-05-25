package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.storage.FileAttributes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by roman on 25.05.17.
 */
public class SimpleQueryTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private FakeMetaDataStore metadataStore;
    private FakeDataSource dataSource;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        metadataStore = new FakeMetaDataStore();
        UUID fileId = UUID.fromString("d5c7bdcb-9286-48d9-bd13-e6bbe1e81652");
        metadataStore.addFileAttributes(new FileAttributes(
                "regedit.exe",
                "C:\\Windows\\",
                fileId,
                new Date(1436471820000L),
                new Date(1473823035000L),
                new Date(1473823035000L),
                null,
                UUID.fromString("dadec7c6-ad8c-4f80-b6da-379fceccd0fc")
        ));
        List<Artifact> artifacts = new LinkedList<Artifact>(){{
            add(new Artifact("test","SGVsbG8gd29ybGQh","base64"));
            add(new Artifact("test","86fb269d190d2c85f6e0468ceca42a20","md5"));
        }};
        metadataStore.addArtifacts(fileId, artifacts);
        dataSource = new FakeDataSource();
    }

    @After
    public void cleanUp() {
        System.setOut(null);
        System.setErr(null);
    }

    @Test
    public void performQuery() throws Exception {
        SimpleQuery q = new SimpleQuery();
        q.setContext(metadataStore, dataSource);
        q.performQuery("regedit");
        Assert.assertTrue(outContent.toString().length() > 0);
    }

}