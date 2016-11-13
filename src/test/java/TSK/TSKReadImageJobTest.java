package TSK;

import ch.hsr.maloney.processing.TSKReadImageJob;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class TSKReadImageJobTest {
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

        TSKReadImageJob tskReadImageJob = new TSKReadImageJob();

        tskReadImageJob.readImage(IMAGE_PATH );
    }
}
