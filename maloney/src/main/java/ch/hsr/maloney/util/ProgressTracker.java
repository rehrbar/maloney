package ch.hsr.maloney.util;

import java.util.Set;

/**
 * Created by olive_000 on 08.11.2016.
 */
public interface ProgressTracker {
    void processInfo(ProgressInfo progressInfo);

    void processInfo(Event event);

    int getProcessedAmount(String type);

    Set<String> getTypes();
}
