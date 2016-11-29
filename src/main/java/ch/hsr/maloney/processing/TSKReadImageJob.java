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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author oniet
 *
 * Reads an Image with help of The Sleuth Kit
 */
public class TSKReadImageJob implements Job {
    private static final String TSK_DB_FILE_EXTENSION = ".db";

    private static final String NEW_FILE_EVENT_NAME = "newFile";
    private static final String NEW_DISK_IMAGE_EVENT_NAME = "newDiskImage";
    private static final String NEW_UNALLOCATED_SPACE_EVENT_NAME = "newUnallocatedSpace";
    private static final String NEW_DIRECTORY_EVENT_NAME = "newDirectory";

    private static final String TSK_READ_IMAGE_JOB_NAME = "TSKReadImageJob";

    private LinkedList<String> producedEvents = new LinkedList<>();
    private LinkedList<String> requiredEvents = new LinkedList<>();
    private final Logger logger;

    public TSKReadImageJob() {
        this.producedEvents.add(NEW_FILE_EVENT_NAME);
        this.producedEvents.add(NEW_UNALLOCATED_SPACE_EVENT_NAME);
        this.producedEvents.add(NEW_DIRECTORY_EVENT_NAME);
        this.requiredEvents.add(NEW_DISK_IMAGE_EVENT_NAME);
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
            addImageToSleuthkitCaseDB(IMAGE_PATH, sk);

            // log information about image
            List<Image> images = sk.getImages();
            for (Image image : images) {
                logger.info("Found image {}", image.getName());
            }

            // push all files into DataSource
            sk.findAllFilesWhere("name NOT IN ('.', '..')").forEach(abstractFile -> { // Low-key SQL Injection
                addToDataSource(ctx, evt, events, abstractFile);
            });
        } catch (TskCoreException e) {
            logger.fatal("Failed to read image in '" + IMAGE_PATH + "' with sleuthkit.", e);
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

    private void addImageToSleuthkitCaseDB(String IMAGE_PATH, SleuthkitCase sk) throws TskCoreException {
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
    }

    private void addToDataSource(Context ctx, Event evt, List<Event> events, AbstractFile abstractFile) {
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
                try {
                    //TODO decide whether to do this here or inside LocalDataSource?
                    if(!Files.exists(WORKING_DIR))
                        Files.createDirectory(WORKING_DIR);

                    logger.debug("Writing file '{}' to '{}'", abstractFile.getName(), file.getPath());
                    is = new ReadContentInputStream(abstractFile);

                    Files.copy(is,file.toPath(), REPLACE_EXISTING);

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

        if(abstractFile.isMetaFlagSet(TskData.TSK_FS_META_FLAG_ENUM.UNALLOC)){
            logger.debug("Creating Event for Unallocated SPAAAACCEEE");
            events.add(new Event(NEW_UNALLOCATED_SPACE_EVENT_NAME, getJobName(), uuid));
        } else if (abstractFile.isFile()) {
            logger.debug("Creating Event for new File");
            events.add(new Event(NEW_FILE_EVENT_NAME, getJobName(), uuid));
        } else if(abstractFile.isDir()){
            logger.debug("Creating Event for new Directory");
            events.add(new Event(NEW_DIRECTORY_EVENT_NAME, getJobName(), uuid));
        }
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
        return TSK_READ_IMAGE_JOB_NAME;
    }

    @Override
    public String getJobConfig() {
        return null;
    }
}
