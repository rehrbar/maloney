package ch.hsr.maloney.storage;

import java.util.*;

/**
 * Created by olive_000 on 08.11.2016.
 */
public class SimpleMetadataStore implements MetadataStore{
    Map<UUID, FileAttributes> uuidFileAttributesMap;

    public SimpleMetadataStore() {
        uuidFileAttributesMap = new HashMap<>();
    }

    @Override
    public FileAttributes getFileAttributes(UUID fileID) {
        return uuidFileAttributesMap.get(fileID);
    }

    @Override
    public void addFileAttributes(FileAttributes fileAttributes) {
        //TODO change signature so that caller does not need to know FileAttributes
        uuidFileAttributesMap.put(fileAttributes.getFileId(), fileAttributes);
    }

    @Override
    public List<Artifact> getArtifacts(UUID fileId) {
        return uuidFileAttributesMap.get(fileId).getArtifacts();
    }

    @Override
    public void addArtifact(UUID fileId, Artifact artifact) {
        uuidFileAttributesMap.get(fileId).addArtifact(artifact);
    }

    @Override
    public void addArtifacts(Map<UUID, Artifact> uuidArtifactMap) {

    }
}
