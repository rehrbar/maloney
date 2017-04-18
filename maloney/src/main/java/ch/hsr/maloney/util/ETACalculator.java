package ch.hsr.maloney.util;

import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by oliver on 17.04.17.
 */
public class ETACalculator {
    private final int RELEVANT_CYCLES;

    private List<Measurement> measurementList;

    public ETACalculator(final int relevantCycles){
        this.RELEVANT_CYCLES = relevantCycles;
        measurementList = new LinkedList<>();
    }

    /**
     * Calculates the average speed over all submitted points
     * @return  Average speed over all submitted points
     */
    public double getAverageSpeed(){
        double avgSpeed = 0;
        int lastFinished = 0;
        long lastTime = 0;

        for (Measurement c : measurementList) {
            // First calculation has to be ignored if it isn't the first time this gets executed
            // Otherwise the it would get miraculous speed
            // e.g.: 0 finished --> 500 finished, when instead actually 460 finished --> 500 finished
            // but wasn't tracked anymore
            if(lastFinished != 0 || lastTime != 0){
                // Add up and later...
                avgSpeed += ((c.getFinished() - lastFinished) / (c.getTime() - lastTime));
            }
            lastFinished = c.getFinished();
            lastTime = c.getTime();
        }
        // ... calculate average (first entry in list does not count)
        if(measurementList.size() == RELEVANT_CYCLES){
            avgSpeed /= (measurementList.size()-1);
        } else {
            avgSpeed /= measurementList.size()-2;
        }
        return avgSpeed;
    }

    /**
     * Calculates an estimated time of arrival(/finish) using the previous processing speeds as defined by relevantCycles
     * @return  Estimated time of arrival/finish
     */
    public LocalDateTime getETA(){
        if(measurementList.size() > 0){
            return LocalDateTime.now().withFieldAdded(
                    DurationFieldType.millis(),
                    (int)(measurementList.get(measurementList.size()-1).getPending() / getAverageSpeed()));
        }
        return null;
    }

    /**
     * Adds a point to the calculator which it uses to later calculate the average
     * @param started       Number of started Jobs
     * @param finished      Number of finished Jobs
     * @param currentTimeInMillis   Current time corresponding to the other parameters
     */
    public void addMeasurement(int started, int finished, long currentTimeInMillis){
        Measurement measurement = new Measurement(started, finished, currentTimeInMillis);
        if(measurementList.size() == RELEVANT_CYCLES){
            measurementList.add(measurement);
            measurementList.remove(0);
        } else {
            measurementList.add(measurement);
        }
    }

    class Measurement {
        final int started;
        final int finished;
        final long time;

        Measurement(int started, int finished, long time) {
            this.started = started;
            this.finished = finished;
            this.time = time;
        }

        int getFinished() {
            return finished;
        }

        int getPending(){
            return started - finished;
        }

        long getTime() {
            return time;
        }
    }
}
