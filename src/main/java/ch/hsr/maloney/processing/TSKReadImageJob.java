package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.Context;
import org.sleuthkit.datamodel.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class TSKReadImageJob implements Job {
    LinkedList<String> producedEvents;

    public TSKReadImageJob(){
        this.producedEvents = new LinkedList<>();
        this.producedEvents.add("newFile");
    }

    public void readImage(String imagePath){

        try {
            SleuthkitCase sk = SleuthkitCase.newCase(imagePath + ".db");

            // initialize the case with an image
            String timezone = "";
            SleuthkitJNI.CaseDbHandle.AddImageProcess process = sk.makeAddImageProcess(timezone, true, false);
            ArrayList<String> paths = new ArrayList<>();
            paths.add(imagePath);
            try {
                process.run(UUID.randomUUID().toString(), paths.toArray(new String[paths.size()]));
            } catch (TskDataException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            process.commit();

            // print out all the images found, and their children
            List<Image> images = sk.getImages();
            for (Image image : images) {
                System.out.println("Found image: " + image.getName());
                System.out.println("There are " + image.getChildren().size() + " children.");
                for (Content content : image.getChildren()) {
                    System.out.println('"' + content.getName() + '"' + " is a child of " + image.getName());
                }
            }

            // print out all .txt files found
            List<AbstractFile> files = sk.findAllFilesWhere("LOWER(name) LIKE LOWER('%.txt')");
            for (AbstractFile file : files) {
                System.out.println("Found text file: " + file.getName());
            }

        } catch (TskCoreException e) {
            System.out.println("Exception caught: " + e.getMessage());
        }
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        readImage(""); //TODO read path parameters
        return null;
    }

    @Override
    public List<String> getRequiredEvents() {
        return null;
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
