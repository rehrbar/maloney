package ch.hsr.maloney.util.query;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.categorization.Category;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SimpleQuery}.
 */
public class SimpleQueryTest {
    private FakeMetaDataStore metadataStore;

    @Before
    public void setUp() {
        metadataStore = new FakeMetaDataStore();
        UUID fileId = UUID.fromString("d5c7bdcb-9286-48d9-bd13-e6bbe1e81652");
        metadataStore.addFileAttributes(new FileAttributes(
                "regedit.exe",
                "C:\\Windows\\",
                fileId,
                new Date(1436471820000L),
                new Date(1473823035000L),
                new Date(1473823035000L),
                UUID.fromString("dadec7c6-ad8c-4f80-b6da-379fceccd0fc")
        ));
        List<Artifact> artifacts = new LinkedList<Artifact>() {{
            add(new Artifact("test", "SGVsbG8gd29ybGQh", "base64"));
            add(new Artifact("test", "86fb269d190d2c85f6e0468ceca42a20", "md5"));
        }};
        metadataStore.addArtifacts(fileId, artifacts);
    }

    @Test
    public void performQuery() throws Exception {
        SimpleQuery q = new SimpleQuery(metadataStore);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        q.performQuery(os, "regedit");
        System.out.println(os.toString());
        assertTrue(os.size() > 0);
    }

    @Test
    public void performQueryById() throws Exception {
        SimpleQuery q = new SimpleQuery(metadataStore);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        q.performQuery(os, "d5c7bdcb");
        System.out.println(os.toString());
        assertTrue(os.size() > 0);
    }

    @Test
    public void performQueryWithExpression() throws Exception {
        SimpleQuery q = new SimpleQuery(metadataStore);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        q.performQuery(os, "fileName=\"reg.*\"");
        System.out.println(os.toString());
        assertTrue(os.size() > 10);
    }

    @Test
    public void performQueryWithArtifactExpression() throws Exception {
        SimpleQuery q = new SimpleQuery(metadataStore);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        q.performQuery(os, "artifactType=\"(?i)MD5\" artifactValue=\"86fb269d190d2c85f6e0468ceca42a20\"");
        System.out.println(os.toString());
        assertTrue(os.size() > 10);
        assertTrue(os.toString().contains("Results: 1"));
    }

    @Test
    public void performQueryWithArtifactExpressionWrongType() throws Exception {
        SimpleQuery q = new SimpleQuery(metadataStore);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        q.performQuery(os, "artifactType=\"SHA1\" artifactValue=\"86fb269d190d2c85f6e0468ceca42a20\"");
        System.out.println(os.toString());
        assertTrue(os.toString().contains("Results: 0"));
    }

    @Test
    public void createQueryCategory() {
        Category c = SimpleQuery.createQueryCategory("fileName=\"reg.*\" filePath=\"(?i).*windows.*\"");
        FileAttributes fileAttributes = new FileAttributes("regex.exe", "C:\\windows\\", null, null, null, null, null);
        Assert.assertTrue(c.getRules().match(fileAttributes));
    }

    @Test
    public void createDateQueryCategory() {
        Category c = SimpleQuery.createQueryCategory("dateCreated=\"2016.*\"");
        // File 1 contains 2016, File2 contains 2015
        FileAttributes file1 = new FileAttributes(null, null, null, null, new Date(1473823035000L), null, null);
        FileAttributes file2 = new FileAttributes(null, null, null, null, new Date(1433823035000L), null, null);
        Assert.assertTrue(c.getRules().match(file1));
        Assert.assertFalse(c.getRules().match(file2));
    }

    @Test
    public void performQueryByIdFiltered() throws Exception {
        SimpleQuery q = new SimpleQuery(metadataStore);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // Inversed order and also reduced output
        q.setFilter("fileName fileId");
        q.performQuery(os, "d5c7bdcb");
        System.out.println(os.toString());
        assertTrue(os.size() > 0);
        assertEquals("fileName\tfileId\t\nregedit.exe\td5c7bdcb-9286-48d9-bd13-e6bbe1e81652\t\nResults: 1\n", os.toString());
    }

}