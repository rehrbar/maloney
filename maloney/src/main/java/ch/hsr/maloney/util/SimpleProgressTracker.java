package ch.hsr.maloney.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oliver on 28.03.17.
 */
public class SimpleProgressTracker implements ProgressTracker {
    private final Logger logger;
    private final Map<ProgressInfoType, Integer> progressMap;

    public SimpleProgressTracker() {
        this.logger = LogManager.getLogger();

        this.progressMap = new HashMap<>();
        for (ProgressInfoType infoType :ProgressInfoType.values()) {
            progressMap.put(infoType,0);
        }
    }

    @Override
    public synchronized void processInfo(ProgressInfo progressInfo) {
        int amount = progressMap.get(progressInfo.getType()) + progressInfo.getAmount();
        progressMap.put(progressInfo.getType(), amount);
    }

    @Override
    public int getProcessedAmount(ProgressInfoType type) {
        return progressMap.get(type);
    }
}
