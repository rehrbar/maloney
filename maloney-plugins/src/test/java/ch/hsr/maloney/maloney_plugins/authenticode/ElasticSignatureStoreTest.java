package ch.hsr.maloney.maloney_plugins.authenticode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by roman on 18.05.17.
 */
public class ElasticSignatureStoreTest {
    ElasticSignatureStore es;
    private List<String> generatedIds;

    @Before
    public void setUp() throws Exception {
        es = new ElasticSignatureStoreTestImpl();
        ElasticSignatureStoreTestImpl e = (ElasticSignatureStoreTestImpl)es;
        e.clearIndex();
        generatedIds = e.seedTestData();
        e.refreshIndex();
    }

    @Test
    public void getSignature() {
        SignatureRecord record = es.getSignature(generatedIds.get(0));
        Assert.assertNotNull(record);
        Assert.assertEquals("b207eaa72396b87a82db095ae73021973bece60a", record.getHash());
        Assert.assertEquals("vboxnetlwf.sys", record.getFilename());
        Assert.assertEquals("2:5.1,2:5.2,2:6.0", record.getOsAttr());
        Assert.assertEquals(CertificateStatus.GOOD, record.getStatus());
        Assert.assertEquals(UUID.fromString("75d856e0-b439-4823-97cf-c3e9b09764e9"), record.getSource());
    }

    @Test
    public void addSignatures() throws Exception {
        SignatureRecord r1 = new SignatureRecord(
                new byte[]{-85, -55, 85, 110, -2, 20, -99, -45, 27, 23, 30, -124, -50, 28, 57, 116, -12, -102, -84, 73},
                "some.exe",
                "2:5.1,2:5.2,2:6.0",
                CertificateStatus.GOOD,
                UUID.fromString("e31d1a37-da85-46a5-ab66-9cada50e29ed")
        );
        SignatureRecord r2 = new SignatureRecord(
                new byte[]{-44, -33, 22, 11, -2, 20, -99, -45, 27, 23, 30, -124, -50, 28, 57, 116, -12, 0, -21, 22},
                "some.exe",
                "2:5.1,2:5.2,2:6.0",
                CertificateStatus.BAD,
                UUID.fromString("2f8e1043-1e6d-457f-9910-2b33f3b98907")
        );
        List<SignatureRecord> signatures = new LinkedList<>();
        signatures.add(r1);
        signatures.add(r2);
        List<String> ids = es.addSignatures(signatures);
        Assert.assertEquals(2, ids.size());
    }

    @Test
    public void findSignatures() throws Exception {
        List<SignatureRecord> results = es.findSignatures("b207eaa72396b87a82db095ae73021973bece60a");
        Assert.assertEquals(1, results.size());

        SignatureRecord firstRecord = results.get(0);
        Assert.assertEquals("vboxnetlwf.sys", firstRecord.getFilename());
    }

    @Test
    public void removeSignature(){
        String id = generatedIds.get(0);
        Assert.assertNotNull(es.getSignature(id));
        es.removeSignature(id);
        Assert.assertNull(es.getSignature(id));
    }

}