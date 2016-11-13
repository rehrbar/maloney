package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobProcessor;
import ch.hsr.maloney.processing.SimpleProcessor;
import ch.hsr.maloney.storage.PlainSource;
import ch.hsr.maloney.storage.SimpleMetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.EventObserver;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Observable;
import java.util.UUID;

/**
 * Created by olive_000 on 25.10.2016.
 */
public class Framework implements EventObserver {
    private JobProcessor jobProcessor;
    private Context context;
    private Object eventQueue; //TODO Better Queue with nice persistence
    private List<Job> registeredJobs;

    public Framework() {
        this.jobProcessor = new SimpleProcessor(context);
        initializeContext();
    }

    private void initializeContext(){
        SimpleMetadataStore simpleMetadataStore = new SimpleMetadataStore();
        this.context = new Context(
                simpleMetadataStore,
                null, //TODO Implement adn add Progress Tracker
                null, //TODO Implement and add Logger
                new PlainSource(simpleMetadataStore)
        );
    }

    public void checkDependencies(){
        //TODO: Not necessary as of now, but later
    }

    public void startWithDisk(String fileName){
        UUID uuid = context.getDataSource().addFile(fileName);
        Event event = new Event("newDiskImage","ch.hsr.maloney.core", uuid);

        registeredJobs.forEach((job -> {
            if(job.getRequiredEvents().isEmpty()){
                //TODO enqueue Jobs in JobProcessor
            }
        }));
    }

    public void register(Job job){
        registeredJobs.add(job);
    }

    public void unregister(Job job){
        if(registeredJobs.contains(job)){
            registeredJobs.remove(job);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof Event){
            update(o, (Event)arg);
        } else {
            throw new IllegalArgumentException("I just don't know, what to doooooo with this type... \uD83C\uDFB6");
        }
    }

    @Override
    public void update(Observable o, Event arg) {
        throw new NotImplementedException();
    }
}
