package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.MetadataStore;

/**
 * Created by olive_000 on 25.10.2016.
 */
public class Context {
    private final MetadataStore metadataStore;
    private final ProgressTracker progressTracker;
    private final DataSource dataSource;

    public Context(MetadataStore metadataStore, ProgressTracker progressTracker, DataSource dataSource) {
        this.metadataStore = metadataStore;
        this.progressTracker = progressTracker;
        this.dataSource = dataSource;
    }

    public MetadataStore getMetadataStore() {
        return metadataStore;
    }

    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
