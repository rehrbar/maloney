package ch.hsr.maloney.core;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by olive_000 on 08.11.2016.
 */
public class FrameworkConfiguration implements Serializable{
    private String JobProcessorType;
    private String MetadataStoreType;
    private String DataSourceType;
    private Map<String, String> JobConfigurationMap;
    private String ProgressTrackerType;
    private String LoggerType;

    public String getJobProcessorType() {
        return JobProcessorType;
    }

    public String getMetadataStoreType() {
        return MetadataStoreType;
    }

    public String getDataSourceType() {
        return DataSourceType;
    }

    public Map<String, String> getJobConfigurationMap() {
        return JobConfigurationMap;
    }

    public String getProgressTrackerType() {
        return ProgressTrackerType;
    }

    public String getLoggerType() {
        return LoggerType;
    }

    public FrameworkConfiguration(String jobProcessorType,
                                  String metadataStoreType,
                                  String dataSourceType,
                                  Map<String, String> jobConfigurationMap,
                                  String progressTrackerType,
                                  String loggerType) {
        JobProcessorType = jobProcessorType;
        MetadataStoreType = metadataStoreType;
        DataSourceType = dataSourceType;
        JobConfigurationMap = jobConfigurationMap;
        ProgressTrackerType = progressTrackerType;
        LoggerType = loggerType;
    }

    public static FrameworkConfiguration loadFromFile(String path){
        File config = new File(path);
        config.canRead();
        return null;
        // return new FrameworkConfiguration();
    }

    public static FrameworkConfiguration loadFromParameters(String params){
        return null;
    }
}
