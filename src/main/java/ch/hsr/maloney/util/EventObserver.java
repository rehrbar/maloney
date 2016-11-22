package ch.hsr.maloney.util;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by olive_000 on 08.11.2016.
 */
public interface EventObserver extends Observer {
    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param o   the observable object.
     * @param arg an argument passed to the <code>notifyObservers</code>
     *            method.
     */
    void update(Observable o, Event arg);
}
