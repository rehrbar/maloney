package ch.hsr.maloney.storage;

import ch.hsr.maloney.util.Event;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;

/**
 * Created by roman on 10.04.17.
 */
public class EventQueue {

    DB db;

    public EventQueue(){
        File file = null;
        try {
            file = File.createTempFile("maloney",".db");
            db = DBMaker.fileDB(file).make();// TODO do we need memory mapped or custom serializer?
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO add queue for running/checked out events

    }

    /**
     * Adds a new event to the store.
     * @param evt Event to add
     */
    public void add(Event evt){
        // TODO implement
    }

    /**
     * Removes the top most element which is not checked.
     * @return Top most element.
     */
    public Event peek(){
        // TODO implement
        return null;
    }

    /**
     * Removes the element from the collection and deletes checkout flag.
     * @param evt
     */
    public void remove(Event evt){
        // TODO implement
    }

    /**
     * Checks wheter the queue is empty or not.
     * @return
     */
    public boolean isEmpty(){
        // TODO implement
        return false;
    }

    /**
     * Closes the underlying db.
     */
    public void close(){
        db.close();
    }
}
