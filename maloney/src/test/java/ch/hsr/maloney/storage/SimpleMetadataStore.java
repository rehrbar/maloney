package ch.hsr.maloney.storage;

import java.util.*;

/**
 * Created by olive_000 on 08.11.2016.
 */
public class SimpleMetadataStore implements MetadataStore {
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
        uuidFileAttributesMap.put(fileAttributes.getFileId(), fileAttributes);
    }

    @Override
    public List<Artifact> getArtifacts(UUID fileId) {
        return uuidFileAttributesMap.get(fileId).getArtifacts();
    }

    @Override
    public void addArtifact(UUID fileId, Artifact artifact) {
        uuidFileAttributesMap.get(fileId).getArtifacts().add(artifact);
    }

    @Override
    public void addArtifacts(UUID fileId, List<Artifact> artifacts) {
        uuidFileAttributesMap.get(fileId).getArtifacts().addAll(artifacts);
    }

    @Override
    public Iterator<UUID> iterator() {
        return uuidFileAttributesMap.keySet().iterator();
    }
}
