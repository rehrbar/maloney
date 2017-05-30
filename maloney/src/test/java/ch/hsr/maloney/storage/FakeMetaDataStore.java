package ch.hsr.maloney.storage;

import java.util.*;

public class FakeMetaDataStore implements MetadataStore {
    Map<UUID, FileAttributes> fileAttributesMap = new HashMap<>();

    @Override
    public FileAttributes getFileAttributes(UUID fileID) {
        return fileAttributesMap.get(fileID);
    }

    @Override
    public void addFileAttributes(FileAttributes fileAttributes) {
        fileAttributesMap.put(fileAttributes.getFileId(), fileAttributes);
    }

    @Override
    public Collection<Artifact> getArtifacts(UUID fileId) {
        return fileAttributesMap.get(fileId).getArtifacts();
    }

    @Override
    public void addArtifact(UUID fileId, Artifact artifact) {
        Collection<Artifact> artifactList = fileAttributesMap.get(fileId).getArtifacts();
        artifactList.add(artifact);
    }

    @Override
    public void addArtifacts(UUID fileId, Collection<Artifact> artifacts) {
        Collection<Artifact> artifactList = fileAttributesMap.get(fileId).getArtifacts();
        artifactList.addAll(artifacts);
    }

    @Override
    public Iterator<FileAttributes> iterator() {
        return fileAttributesMap.values().iterator();
    }

}
