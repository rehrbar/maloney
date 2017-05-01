package ch.hsr.maloney.storage;

import java.util.*;

public class FakeMetaDataStore implements MetadataStore {

    Map<UUID, List<Artifact>> artifacts = new HashMap<>();
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
    public Iterator<FileAttributes> iterator() {
        return new Iterator<FileAttributes>() {
            Iterator<UUID> iterator = fileAttributesMap.keySet().iterator();
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public FileAttributes next() {
                return fileAttributesMap.get(iterator.next());
            }
        };
    }

}
