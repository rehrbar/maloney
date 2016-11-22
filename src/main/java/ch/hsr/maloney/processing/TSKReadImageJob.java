package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.*;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sleuthkit.datamodel.*;

import java.io.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class TSKReadImageJob implements Job {
    private static final String TSK_DB_FILE_EXTENSION = ".db";
    private static final int BUFFER_SIZE = 512;
    private final String NewFileEventName = "newFile";
    private final String NewDiskImageEventName = "newDiskImage";

    private LinkedList<String> producedEvents = new LinkedList<>();
    private LinkedList<String> requiredEvents = new LinkedList<>();
    private final Logger logger;

    public TSKReadImageJob(){
        this.producedEvents.add(NewFileEventName);
        this.requiredEvents.add(NewDiskImageEventName);
        this.logger = LogManager.getLogger();
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        for(String eventName : requiredEvents){
            if(evt.getName().equals(eventName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        DataSource dataSource = ctx.getDataSource();
        java.io.File file = dataSource.getFile(evt.getFileUuid());

        final String IMAGE_PATH = file.getAbsolutePath();
        List<Event> events = new LinkedList<>();

        try {
            SleuthkitCase sk;
            sk = getSleuthkitCase(dataSource);

            // initialize the case with an image
            String timezone = "";
            SleuthkitJNI.CaseDbHandle.AddImageProcess process = sk.makeAddImageProcess(timezone, true, false);
            ArrayList<String> paths = new ArrayList<>();
            paths.add(IMAGE_PATH);
            try {
                process.run(UUID.randomUUID().toString(), paths.toArray(new String[paths.size()]));
            } catch (TskDataException ex) {
                logger.error("Could not add image {}", IMAGE_PATH, ex);
            }
            process.commit();

            // log information about image
            List<Image> images = sk.getImages();
            for (Image image : images) {
                logger.info("Found image {}", image.getName());
            }

            // push all files into DataSource
            sk.findAllFilesWhere("1=1").forEach(abstractFile -> { // Low-key SQL Injection
                addToDataSource(ctx, evt, events, abstractFile, sk);
            });
        } catch (TskCoreException e) {
            logger.fatal("Failed to read image with sleuthkit.", e);
        }

        return events;
    }

    private SleuthkitCase getSleuthkitCase(DataSource dataSource) throws TskCoreException {
        SleuthkitCase sk;
        final Path TSK_DB_LOCATION = Paths.get(dataSource.getJobWorkingDir(this.getClass()) + TSK_DB_FILE_EXTENSION);

        try{
            if(!Files.exists(TSK_DB_LOCATION)){
                SleuthkitCase.newCase(TSK_DB_LOCATION.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        sk = SleuthkitCase.openCase(TSK_DB_LOCATION.toString());
        return sk;
    }

    private void addToDataSource(Context ctx, Event evt, List<Event> events, AbstractFile abstractFile, SleuthkitCase sk) {
        DataSource dataSource = ctx.getDataSource();

        UUID uuid = dataSource.addFile(evt.getFileUuid(), new FileExtractor() {
            List<File> extractedFiles;

            @Override
            public boolean useOriginalFile() {
                return false;
            }

            @Override
            public Path extractFile() {
                //TODO get single file from TSK and put into working dir
                //TODO all files are saved in flat structure, rebuild structure in working directory?
                final Path WORKING_DIR = dataSource.getJobWorkingDir(TSKReadImageJob.class);

                // Get Location where the file has to be saved
                java.io.File file = new File(WORKING_DIR + "" + abstractFile.getId());
                //"" Because otherwise it won't recognize it as a string
                if(extractedFiles == null){
                    extractedFiles = new LinkedList<>();
                }
                extractedFiles.add(file);

//                try {
//                    Files.deleteIfExists(Paths.get(file.getPath()));
//                } catch (IOException e) {
//                    logger.error("Failed while trying to delete already existing File: {}", abstractFile.getName(), e);
//                }

                // Write specified File into working directory
                try {
                    FileOutputStream os = new FileOutputStream(file);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    long offset = 0;
                    long length = (long) BUFFER_SIZE;

                    while(abstractFile.read(buffer, offset, length) > 0){
                        os.write(buffer);
                        offset += BUFFER_SIZE;
                    }
                } catch (TskCoreException e) {
                    logger.error("Error while trying to read file {}", abstractFile.getName(), e);
                } catch (IOException e) {
                    logger.error("Could not write into working copy file: {}", abstractFile.getName(), e);
                }

                return Paths.get(file.getPath());
            }

            @Override
            public FileSystemMetadata extractMetadata() {
                return new FileSystemMetadata(
                        abstractFile.getName(),
                        abstractFile.getLocalAbsPath(),
                        new Date(abstractFile.getCrtime()),
                        new Date(abstractFile.getMtime()),
                        new Date(abstractFile.getAtime()),
                        abstractFile.getSize()
                        );
            }

            @Override
            public void cleanup() {
                if(extractedFiles != null){
                    extractedFiles.forEach(file -> {
                        try {
                            Files.deleteIfExists(Paths.get(file.getPath()));
                        } catch (IOException e) {
                            logger.warn("Could not delete file '{}'", file.getName(), e);
                        }
                    });
                }
            }
        });

        events.add(new Event(NewFileEventName, getJobName(), uuid));
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
