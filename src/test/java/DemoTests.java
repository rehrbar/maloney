import ch.hsr.maloney.core.Demo;
import org.junit.Test;
import org.sleuthkit.datamodel.Examples.Sample;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by r1ehrbar on 17.10.2016.
 */
public class DemoTests {

    @Test
    public void helloWorldTest() {
        assertEquals("Hello World!", Demo.helloWorld());
    }

    @Test
    public void tskTest() {
        assertEquals("Name.Something", Demo.testTsk());
    }

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

//        Demo.tskSample(path);
        Sample.run(IMAGE_PATH);


    }
}
