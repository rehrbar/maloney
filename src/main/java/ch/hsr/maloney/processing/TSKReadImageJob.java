package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
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
    private LinkedList<String> producedEvents = new LinkedList<>();
    private LinkedList<String> requiredEvents = new LinkedList<>();

    public TSKReadImageJob(){
        this.producedEvents.add("newFile");
        this.requiredEvents.add("newImage");
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        java.io.File file = ctx.getDataSource().getFile(evt.getFileUuid());
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
                ctx.getLogger().logError(this.getJobName() + ": Could not add image " + IMAGE_PATH, ex);
            }
            process.commit();

            // add all files found inside the image to the MetaDataStore
            List<Image> images = sk.getImages();
            for (Image image : images) {
                ctx.getLogger().logInfo(this.getJobName() + ": Found image " + image.getName());
                ctx.getLogger().logInfo(this.getJobName() + ": There are " + image.getChildren().size() + " children.");
            }

            // push all files into MetaDataStore
            sk.findAllFilesWhere("1=1").forEach(abstractFile -> { // Low-key SQL Injection
                pushToMetaDataStore(ctx, evt, events, abstractFile);
            });
        } catch (TskCoreException e) {
            ctx.getLogger().logFatal(this.getJobName() + ": Failed with Exception", e);
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
            ctx.getLogger().logError(
                    this.getJobName() + ": Couldn't read Unique Path from file " + abstractFile.getName(), e
            );
        }
        events.add(new Event("fileAdded",this.getJobName(),uuid));
        ctx.getLogger().logInfo(this.getJobName() + ": Added \"" + abstractFile.getName() + "\" to MetaDataStore");
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
