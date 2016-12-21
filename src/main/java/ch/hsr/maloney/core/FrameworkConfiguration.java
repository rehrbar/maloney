package ch.hsr.maloney.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the configuration for the framework to set everything up.
 */
public class FrameworkConfiguration implements Serializable {
    static ObjectMapper mapper = new ObjectMapper();
    private String jobProcessorType;
    private String metadataStoreType;
    private String dataSourceType;
    private String workingDirectory;
    private Map<String, String> jobConfigurationMap = new HashMap<>();
    private String progressTrackerType;

    public static FrameworkConfiguration loadFromFile(String path) throws IOException {
        File config = new File(path);
        return mapper.readValue(config, FrameworkConfiguration.class);
    }

    public static FrameworkConfiguration loadFromParameters(String[] args) {
        return null;
    }

    public void saveToFile(String path) throws IOException {
        mapper.writeValue(new File(path), this);
    }

    public String getJobProcessorType() {
        return jobProcessorType;
    }

    public void setJobProcessorType(String jobProcessorType) {
        this.jobProcessorType = jobProcessorType;
    }

    public String getMetadataStoreType() {
        return metadataStoreType;
    }

    public void setMetadataStoreType(String metadataStoreType) {
        this.metadataStoreType = metadataStoreType;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public Map<String, String> getJobConfigurationMap() {
        return jobConfigurationMap;
    }

    public void setJobConfigurationMap(Map<String, String> jobConfigurationMap) {
        this.jobConfigurationMap = jobConfigurationMap;
    }

    public String getProgressTrackerType() {
        return progressTrackerType;
    }

    public void setProgressTrackerType(String progressTrackerType) {
        this.progressTrackerType = progressTrackerType;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}
