package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.FileExtractor;
import ch.hsr.maloney.storage.FileSystemMetadata;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sleuthkit.datamodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class TSKReadImageJob implements Job {
    private static final String TSK_DB_FILE_EXTENSION = ".db";
    private final String NewFileEventName = "newFile";
    private final String NewDiskImageEventName = "newDiskImage";

    private LinkedList<String> producedEvents = new LinkedList<>();
    private LinkedList<String> requiredEvents = new LinkedList<>();
    private final Logger logger;

    public TSKReadImageJob() {
        this.producedEvents.add(NewFileEventName);
        this.requiredEvents.add(NewDiskImageEventName);
        this.logger = LogManager.getLogger();
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        for (String eventName : requiredEvents) {
            if (evt.getName().equals(eventName)) {
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

        try {
            if (!Files.exists(TSK_DB_LOCATION)) {
                SleuthkitCase.newCase(TSK_DB_LOCATION.toString());
            }
        } catch (Exception e) {
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
                logger.info("Extratcting file to {}", WORKING_DIR);

                // Get Location where the file has to be saved
                java.io.File file = WORKING_DIR.resolve(Long.toString(abstractFile.getId())).toFile();

                ReadContentInputStream is = null;
                FileOutputStream os = null;
                try {
                    //TODO decide whether to do this here or inside LocalDataSource?
                    if(!Files.exists(WORKING_DIR))
                        Files.createDirectory(WORKING_DIR);

                    logger.debug("Writing file '{}' to '{}'", abstractFile.getName(), file);
                    is = new ReadContentInputStream(abstractFile);
                    os = new FileOutputStream(file);

                    int read = 0;
                    byte[] bytes = new byte[512];

                    while ((read = is.read(bytes)) != -1) {
                        os.write(bytes, 0, read);
                    }

                    if (extractedFiles == null) {
                        extractedFiles = new LinkedList<>();
                    }
                    extractedFiles.add(file);

                    logger.debug("Done writing file '{}'", abstractFile.getName());
                } catch (IOException e) {
                    logger.error("Could not write file '" + abstractFile.getName() + "' to temporary dir '" +
                            file.getAbsolutePath() + "'.", e);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (os != null) {
                            os.close();
                        }
                    } catch (IOException e){
                            logger.error("Could not close IOstream(s)", e);
                    }
                }

                return Paths.get(file.getPath());
            }

            @Override
            public FileSystemMetadata extractMetadata() {
                return new FileSystemMetadata(
                        abstractFile.getName(),
                        abstractFile.getParentPath(),
                        new Date(abstractFile.getCrtime()),
                        new Date(abstractFile.getMtime()),
                        new Date(abstractFile.getAtime()),
                        abstractFile.getSize()
                );
            }

            @Override
            public void cleanup() {
                if (extractedFiles != null) {
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
