import ch.hsr.maloney.processing.TSKAdapter;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class TSKAdapterTest {
    @Test
    public void simpleTest(){
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

        TSKAdapter tskAdapter = new TSKAdapter();

        tskAdapter.readImage(IMAGE_PATH );
    }
}
