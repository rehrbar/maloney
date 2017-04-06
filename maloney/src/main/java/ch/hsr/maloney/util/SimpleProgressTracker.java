package ch.hsr.maloney.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by oliver on 28.03.17.
 */
public class SimpleProgressTracker implements ProgressTracker {
    private final Logger logger;
    private final Map<String, Integer> progressMap;

    public SimpleProgressTracker() {
        this.logger = LogManager.getLogger();

        this.progressMap = new HashMap<>();
    }

    @Override
    public void processInfo(ProgressInfo progressInfo) {
        update(progressInfo.getType().toString(), progressInfo.getAmount());
    }

    public void processInfo(Event event){
        update(event.getName(), 1);
    }

    private synchronized void update(String type, int amount) {
        if(progressMap.get(type) != null){
            int newAmount = progressMap.get(type) + amount;
            progressMap.put(type, newAmount);
        } else {
            progressMap.put(type, amount);
        }
    }



    @Override
    public int getProcessedAmount(String type) {
        return progressMap.get(type);
    }

    @Override
    public Set<String> getTypes() {
        return progressMap.keySet();
    }
}
