package ch.hsr.maloney.maloney_plugins.authenticode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

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
    public void addSignatures() throws Exception {
        SignatureRecord r1 = new SignatureRecord(
                new byte[]{22,33,44,11},
                "some.exe",
                null,
                CertificateStatus.GOOD,
                UUID.fromString("e31d1a37-da85-46a5-ab66-9cada50e29ed")
        );
        SignatureRecord r2 = new SignatureRecord(
                new byte[]{22,33,44,11},
                "some.exe",
                null,
                CertificateStatus.GOOD,
                UUID.fromString("e31d1a37-da85-46a5-ab66-9cada50e29ed")
        );
        List<SignatureRecord> signatures = new LinkedList<>();
        signatures.add(r1);
        signatures.add(r2);
        es.addSignatures(signatures);
        // TODO add assertions
    }

    @Test
    public void findSignatures() throws Exception {
        // TODO add assertions
    }

}