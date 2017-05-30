package ch.hsr.maloney.core;

import ch.hsr.maloney.storage.EventStore;
import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.FakeProgressTracker;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FakeFramework extends Framework{
    public FakeFramework(boolean persistent){
        this(persistent, new FakeMetaDataStore());
    }

    public FakeFramework(boolean persistent, MetadataStore metadataStore){
        super(new EventStore(getWorkingDir(), persistent), new Context(metadataStore, new FakeProgressTracker(), new FakeDataSource(metadataStore), null));
    }

    protected static Path getWorkingDir(){
        Path workingDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "maloney_test");
        try {
            Files.createDirectories(workingDirectory);
            FileUtils.forceDeleteOnExit(workingDirectory.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workingDirectory;
    }

    public static void deleteEventStore(){
        try {
            // TODO fix delete method
            Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"), "maloney-events.db"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
