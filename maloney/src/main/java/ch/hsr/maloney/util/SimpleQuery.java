package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.storage.MetadataStore;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SimpleQuery {
    protected MetadataStore metadataStore;
    protected DataSource dataSource;

    public void setContext(MetadataStore metadataStore, DataSource dataSource){
        this.metadataStore = metadataStore;
        this.dataSource = dataSource;
    }

    public void performQuery(String query){
        // TODO create custom category
        // TODO loop through all files and apply category
        Iterator<FileAttributes> iterator = metadataStore.iterator();
        while (iterator.hasNext()){
            FileAttributes fileAttributes = iterator.next();
            List<Artifact> artifacts = metadataStore.getArtifacts(fileAttributes.getFileId());

            if(isMatch(query, fileAttributes, artifacts)){
                writeToOutput(fileAttributes, artifacts);
            }
        }
    }

    protected void writeToOutput(FileAttributes fileAttributes, List<Artifact> artifacts){
        StringBuilder sb = new StringBuilder();
        // TODO format output
        // TODO filter fields
        sb.append(fileAttributes.getFileId());
        System.out.println(sb.toString());
    }

    protected boolean isMatch(String query, FileAttributes fileAttributes, List<Artifact> artifacts) {
        boolean artifactMatch = artifacts.stream().anyMatch(a -> a.getType().contains(query)
                        || a.getOriginator().contains(query)
                        || a.getValue().toString().contains(query));
        return fileAttributes.getFileName().contains(query)
                || fileAttributes.getFilePath().contains(query)
                || artifactMatch;
    }
}
