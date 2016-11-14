package TSK;

import ch.hsr.maloney.processing.TSKReadImageJob;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.storage.PlainSource;
import ch.hsr.maloney.storage.SimpleMetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.ToConsoleLogger;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

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
        MetadataStore metadataStore = new SimpleMetadataStore();

        tskReadImageJob.run(new Context(metadataStore , null, new ToConsoleLogger(), new PlainSource(metadataStore)),
                new Event("newImage","Test", UUID.randomUUID()));
    }
}
