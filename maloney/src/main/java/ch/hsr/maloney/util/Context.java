package ch.hsr.maloney.util;

import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.categorization.CategoryService;

/**
 * Created by olive_000 on 25.10.2016.
 */
public class Context {
    private final MetadataStore metadataStore;
    private final ProgressTracker progressTracker;
    private final DataSource dataSource;
    private final CategoryService categoryService;

    public Context(MetadataStore metadataStore, ProgressTracker progressTracker, DataSource dataSource, CategoryService categoryService) {
        this.metadataStore = metadataStore;
        this.progressTracker = progressTracker;
        this.dataSource = dataSource;
        this.categoryService = categoryService;
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

    public CategoryService getCategoryService() {
        return categoryService;
    }
}
