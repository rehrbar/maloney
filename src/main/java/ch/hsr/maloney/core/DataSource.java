package ch.hsr.maloney.core;

/**
 * Created by olive_000 on 01.11.2016.
 */
public interface DataSource<T> {
    void registerFileAttributes();
    T getFile(String fileID);
    void addFile(String path);
}
