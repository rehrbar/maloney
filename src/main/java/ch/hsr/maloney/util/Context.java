package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.MetadataStore;

/**
 * Created by olive_000 on 25.10.2016.
 */
public class Context {
    private MetadataStore metadataStore;
    private ProgressTracker progressTracker;
    private Logger logger;
    private DataSource dataSource;

    public Context(MetadataStore metadataStore, ProgressTracker progressTracker, Logger logger, ch.hsr.maloney.storage.DataSource dataSource) {
        this.metadataStore = metadataStore;
        this.progressTracker = progressTracker;
        this.logger = logger;
        this.dataSource = dataSource;
    }

    public MetadataStore getMetadataStore() {
        return metadataStore;
    }

    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public Logger getLogger() {
        return logger;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
