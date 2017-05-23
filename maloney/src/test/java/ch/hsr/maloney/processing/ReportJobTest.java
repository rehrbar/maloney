package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.*;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FakeProgressTracker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

/**
 * Created by oliver on 02.05.17.
 */
public class ReportJobTest {
    private Context ctx;

    @Before
    public void setUp(){
        MetadataStore metadataStore = new SimpleMetadataStore();
        ctx = new Context(metadataStore, new FakeProgressTracker(), new PlainSource(metadataStore), null);
    }

    private void seedTestData(int entries) {
        DataSource dataSource = ctx.getDataSource();

        for(int i = 0; i < entries; i++){
            UUID randomUuid = UUID.randomUUID();
            dataSource.addFile(null, new FileExtractor() {
                @Override
                public boolean useOriginalFile() {
                    return false;
                }

                @Override
                public Path extractFile() {
                    // the file should not be extracted
                    return null;
                }

                @Override
                public FileSystemMetadata extractMetadata() {
                    //TODO create random test data
                    return new FileSystemMetadata(
                            randomUuid.toString(),
                            "/dev/null",
                            new Date(),
                            new Date(),
                            new Date(),
                            0);
                }

                @Override
                public void cleanup() {
                    // hasn't to do anything, because it will not be extracted
                }
            });
        }

    }

    @After
    public void cleanUp(){
        ((PlainSource)ctx.getDataSource()).cleanUp();
    }

    @Test
    public void simpleRun() throws Exception {
        simpleTest(10);
    }

    @Test
    public void largeRun() throws Exception {
        simpleTest(1000);
    }

    @Test
    public void emptyRun() throws Exception {
        simpleTest(0);
    }

    private void simpleTest(int numberOfEntries) throws JobCancelledException {
        ReportJob reportJob = new ReportJob();
        seedTestData(numberOfEntries);

        UUID uuid = UUID.randomUUID();
        reportJob.run(ctx, new Event("Test","Test",uuid));

        // +2 because of header
        Assert.assertEquals(numberOfEntries + 2, getLinesInReport(reportJob));
    }

    private int getLinesInReport(ReportJob reportJob) {
        int counter = 0;
        for (File file : ctx.getDataSource().getJobWorkingDir(reportJob.getClass()).toFile().listFiles()) {
            try(BufferedReader bufferedReader = Files.newBufferedReader(file.toPath())){
                while(bufferedReader.readLine() != null){
                    counter++;
                }
            } catch (IOException e){
                Assert.fail("Report not found");
            }
        }
        return counter;
    }

}