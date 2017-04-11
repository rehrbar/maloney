package ch.hsr.maloney.processing;

import ch.hsr.maloney.core.FrameworkEventNames;
import ch.hsr.maloney.storage.*;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static ch.hsr.maloney.core.Framework.EVENT_ORIGIN;

/**
 * Created by roman on 03.12.16.
 */
public class DiskImageJobTest {
    private FakeMetaDataStore fakeMetaDataStore;
    private FakeDataSource fakeDataSource;
    private Context ctx;

    private Path testImage;
    @Before
    public void setup(){
        fakeMetaDataStore = new FakeMetaDataStore();
        fakeDataSource = new FakeDataSource();
        ctx = new Context(fakeMetaDataStore, null, fakeDataSource);
    }

    @After
    public void cleanup() throws IOException {
        Files.deleteIfExists(testImage);
    }

    @Test
    public void run() throws IOException {
        Event startupEvent = new Event(FrameworkEventNames.STARTUP, EVENT_ORIGIN, null);
        testImage = Files.createTempFile("maloney", null);
        Job job = new DiskImageJob();
        job.setJobConfig(testImage.toString());
        Assert.assertTrue(job.canRun(ctx, startupEvent));
        job.run(ctx, startupEvent);
        Assert.assertTrue(fakeDataSource.getFiles().contains(testImage));
    }
}
