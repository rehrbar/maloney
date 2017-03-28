package TSK;

import org.junit.Assert;
import org.junit.Test;
import org.sleuthkit.datamodel.Examples.Sample;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class TSKIntegrationTest {
    @Test
    public void tskTestSample() {
        final String IMAGE_PATH = "..\\images\\autopsy-demo-disk.dd";

        try{
            Files.deleteIfExists(Paths.get(IMAGE_PATH + ".db"));
        }catch (Exception e){
            e.printStackTrace();
        }

        System.loadLibrary("zlib");
        System.loadLibrary("libewf");
        System.loadLibrary("libvmdk");
        System.loadLibrary("libvhdi");
        System.loadLibrary("libtsk_jni");

        Sample.run(IMAGE_PATH);

        Assert.assertTrue(Files.exists(Paths.get(IMAGE_PATH + ".db")));
    }
}
