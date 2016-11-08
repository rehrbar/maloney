package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobProcessor;
import ch.hsr.maloney.util.Context;

import java.util.List;

/**
 * Created by olive_000 on 25.10.2016.
 */
public class Framework {
    private JobProcessor jobProcessor;
    private Context context;
    private Object eventQueue; //TODO Better Queue with nice persistence
    private List<Job> registeredJobs;

    public Framework(JobProcessor jobProcessor, Context context) {
        this.jobProcessor = jobProcessor;
        this.context = context;
        initialize();
    }

    private void initialize() {

        checkDependencies();
    }

    private void checkDependencies(){

    }

    public void startWithDisk(String fileName){

    }
}
