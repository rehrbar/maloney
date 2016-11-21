package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sleuthkit.datamodel.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class TSKReadImageJob implements Job {
    private final String NewFileEventName = "newFile";
    private final String NewDiskImageEventName = "newDiskImage";

    private LinkedList<String> producedEvents = new LinkedList<>();
    private LinkedList<String> requiredEvents = new LinkedList<>();
    final Logger logger;

    public TSKReadImageJob(){
        this.producedEvents.add(NewFileEventName);
        this.requiredEvents.add(NewDiskImageEventName);
        this.logger = LogManager.getLogger();
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        DataSource dataSource = ctx.getDataSource();
        java.io.File file = dataSource.getFile(evt.getFileUuid());
        final String IMAGE_PATH = file.getAbsolutePath();
        List<Event> events = new LinkedList<>();

        try {
            //TODO how to manage the TSK DB
            // where should we put it?
            // delete it afterwards?
            SleuthkitCase sk;
            try{
                if(!Files.exists(Paths.get(IMAGE_PATH + ".db"))){
                    SleuthkitCase.newCase(IMAGE_PATH + ".db");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            sk = SleuthkitCase.openCase(IMAGE_PATH + ".db");

            // initialize the case with an image
            String timezone = "";
            SleuthkitJNI.CaseDbHandle.AddImageProcess process = sk.makeAddImageProcess(timezone, true, false);
            ArrayList<String> paths = new ArrayList<>();
            paths.add(IMAGE_PATH);
            try {
                process.run(UUID.randomUUID().toString(), paths.toArray(new String[paths.size()]));
            } catch (TskDataException ex) {
                logger.error("Could not add image " + IMAGE_PATH, ex);
            }
            process.commit();

            // add all files found inside the image to the MetaDataStore
            List<Image> images = sk.getImages();
            for (Image image : images) {
                logger.info("Found image {}", image.getName());
                logger.info("There are {} children.", image.getChildren().size());
            }

            // push all files into MetaDataStore
            sk.findAllFilesWhere("1=1").forEach(abstractFile -> { // Low-key SQL Injection
                pushToMetaDataStore(ctx, evt, events, abstractFile);
            });
        } catch (TskCoreException e) {
            logger.fatal("Failed to read image with sleuthkit.", e);
        }

        //TODO create and return events
        return events;
    }

    private void pushToMetaDataStore(Context ctx, Event evt, List<Event> events, AbstractFile abstractFile) {
        UUID uuid = UUID.randomUUID();//TODO add to DataSource
        try {
            ctx.getMetadataStore().addFileAttributes(
                    new FileAttributes( //TODO add crated, edited, changed etc. stamps
                            abstractFile.getName(),
                            abstractFile.getUniquePath(),
                            uuid,
                            null,
                            null,
                            null,
                            null,
                            evt.getFileUuid()
                    )
            );
        } catch (TskCoreException e) {
            logger.error(this.getJobName() + ": Couldn't read Unique Path from file " + abstractFile.getName(), e);
        }
        events.add(new Event("fileAdded",this.getJobName(),uuid));
        logger.info("Added \"{}\" to MetaDataStore", abstractFile.getName());
    }

    @Override
    public List<String> getRequiredEvents() {
        return requiredEvents;
    }

    @Override
    public List<String> getProducedEvents() {
        return producedEvents;
    }

    @Override
    public String getJobName() {
        return "TSKReadImageJob";
    }

    @Override
    public String getJobConfig() {
        return null;
    }
}
