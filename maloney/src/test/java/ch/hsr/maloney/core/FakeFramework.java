package ch.hsr.maloney.core;

import ch.hsr.maloney.storage.EventStore;
import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.util.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FakeFramework extends Framework{
    public FakeFramework(boolean persistent){
        super(new EventStore(persistent));
    }

    @Override
    protected void initializeContext() {
        // TODO replace with parameters passed by framework controller
        // TODO create fake progress tracker and add it
        this.context = new Context(new FakeMetaDataStore(), null, new FakeDataSource());
    }

    public static void deleteEventStore(){
        try {
            Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"), "maloney-events.db"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
