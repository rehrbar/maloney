package ch.hsr.maloney.util;

/**
 * Created by olive_000 on 08.11.2016.
 */
public interface ProgressTracker {
    void processInfo(ProgressInfo progressInfo);

    int getProcessedAmount(ProgressInfoType type);
}
