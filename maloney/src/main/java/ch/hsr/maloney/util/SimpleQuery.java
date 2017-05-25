package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.storage.MetadataStore;

import java.util.Iterator;
import java.util.List;

// TODO probably replace through reporting feature
public class SimpleQuery {
    protected MetadataStore metadataStore;
    protected DataSource dataSource;

    public void setContext(MetadataStore metadataStore, DataSource dataSource){
        this.metadataStore = metadataStore;
        this.dataSource = dataSource;
    }

    public void setFilter(String filter){
        // TODO do something with this filter
    }

    public void performQuery(String query){
        int counter = 0;
        // TODO create custom category
        // TODO loop through all files and apply category
        Iterator<FileAttributes> iterator = metadataStore.iterator();
        while (iterator.hasNext()){
            FileAttributes fileAttributes = iterator.next();
            List<Artifact> artifacts = metadataStore.getArtifacts(fileAttributes.getFileId());

            if(isMatch(query, fileAttributes, artifacts)){
                writeToOutput(fileAttributes, artifacts);
                counter++;
            }
        }
        System.out.println("Results: "+ counter);
    }

    protected void writeToOutput(FileAttributes fileAttributes, List<Artifact> artifacts){
        StringBuilder sb = new StringBuilder();
        // TODO format output
        // TODO filter fields
        sb.append(fileAttributes.getFileId());
        sb.append(" ");
        sb.append(fileAttributes.getFileName());
        System.out.println(sb.toString());
        // TODO replace sout with output stream
    }

    protected boolean isMatch(String query, FileAttributes fileAttributes, List<Artifact> artifacts) {
        boolean artifactMatch = artifacts.stream().anyMatch(a -> a.getType().contains(query)
                        || a.getOriginator().contains(query)
                        || a.getValue().toString().contains(query));
        return (fileAttributes.getFileName() != null && fileAttributes.getFileName().contains(query))
                || (fileAttributes.getFilePath() != null && fileAttributes.getFilePath().contains(query))
                || artifactMatch;
    }
}
