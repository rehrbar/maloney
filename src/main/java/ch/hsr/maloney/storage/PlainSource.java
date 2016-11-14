package ch.hsr.maloney.storage;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.UUID;

/**
 * Created by olive_000 on 08.11.2016.
 */
public class PlainSource implements DataSource {
    MetadataStore metadataStore;

    public PlainSource(MetadataStore metadataStore) {
        this.metadataStore = metadataStore;
    }

    @Override
    public void registerFileAttributes() {
        //Not necessary as of now
        //TODO necessary?
    }

    @Override
    public File getFile(UUID fileID) {
        return new File(metadataStore.getFileAttributes(fileID).getFilePath());
    }

    @Override
    public FileInputStream getFileStream(UUID fileID) {
        //TODO get as FileStream
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID addFile(String path) {
        UUID uuid = UUID.randomUUID();
        metadataStore.addFileAttributes(new FileAttributes("image",path,uuid,new Date(),new Date(),new Date(), null));
        return uuid;
    }
}
