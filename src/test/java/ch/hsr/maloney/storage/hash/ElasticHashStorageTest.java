package ch.hsr.maloney.storage.hash;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

public class ElasticHashStorageTest {
    ElasticHashStore es;

    @Before
    public void setUp() throws UnknownHostException {
        es = new ElasticHashStorageTestImpl();
        ((ElasticHashStorageTestImpl)es).clearIndex();
        ((ElasticHashStorageTestImpl)es).seedTestData();
        ((ElasticHashStorageTestImpl)es).refreshIndex();
    }

    @Test
    public void testIndexHash(){
        HashRecord record = new HashRecord(HashType.GOOD, "rds_254u", "Windows", "Canvas 8");
        record.setUpdated(new Date());
        record.getHashes().put(HashAlgorithm.MD5, "392126E756571EBF112CB1C1CDEDF926");
        record.getHashes().put(HashAlgorithm.SHA1, "000000206738748EDD92C4E3D2E823896700F849");
        record.getHashes().put(HashAlgorithm.CRC32, "EBD105A0");

        String id = es.addHashRecord(record);
        Map<String, Object> dump = ((ElasticHashStorageTestImpl)es).dumpRecord(id);
        printMap(dump);
        Assert.assertTrue(dump != null);
        Assert.assertEquals(record.getType(), HashType.valueOf((String)dump.get("type")));
        Assert.assertEquals(3, ((Map<Object, Object>)dump.get("hashes")).size());
    }

    private void printMap(Map<String, Object> map){
        System.out.println("Content of map:");
        map.forEach((k, v) -> {
            System.out.printf(" - %s => %s\n", k, v== null? "n/a" : v.toString());
        });
    }
}
