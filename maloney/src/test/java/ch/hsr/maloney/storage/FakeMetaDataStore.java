package ch.hsr.maloney.storage;

import java.util.*;

public class FakeMetaDataStore implements MetadataStore {

    Map<UUID, List<Artifact>> artifacts = new HashMap<>();

    @Override
    public FileAttributes getFileAttributes(UUID fileID) {
        return null;
    }

    @Override
    public void addFileAttributes(FileAttributes fileAttributes) {

    }

    @Override
    public List<Artifact> getArtifacts(UUID fileId) {
        return artifacts.get(fileId);
    }

    @Override
    public void addArtifact(UUID fileId, Artifact artifact) {
        List<Artifact> artifactList = artifacts.get(fileId);
        if (artifactList == null) {
            artifactList = new LinkedList<>();
        }
        artifactList.add(artifact);
        artifacts.put(fileId, artifactList);
    }

    @Override
    public void addArtifacts(UUID fileId, List<Artifact> artifacts) {
        for (Artifact a : artifacts) {
            addArtifact(fileId, a);
        }
    }

    @Override
    public Iterator<UUID> iterator() {
        return artifacts.keySet().iterator();
    }

}
