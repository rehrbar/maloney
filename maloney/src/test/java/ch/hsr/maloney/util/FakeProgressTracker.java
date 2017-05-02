package ch.hsr.maloney.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oliver on 06.04.17.
 */
public class FakeProgressTracker implements ProgressTracker {
    Set<String> types = new HashSet<>();

    @Override
    public void processInfo(ProgressInfo progressInfo) {

    }

    @Override
    public void processInfo(Event event) {

    }

    @Override
    public int getProcessedAmount(String type) {
        return 0;
    }

    @Override
    public Set<String> getTypes() {
        if(types == null){
            types = new HashSet<>();
        }
        return types;
    }
}
