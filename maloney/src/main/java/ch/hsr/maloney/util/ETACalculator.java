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
     * @return  Average speed over all submitted points in task per millisecond.
     */
    public double getAverageSpeed(){
        if(measurementList.size() < 2){
            return 0.0;
        }

        double speedSum = 0;
        int lastFinished = 0;
        long lastTime = 0;
        int avgElements = 0;

        for (Measurement c : measurementList) {
            // First calculation has to be ignored if it isn't the first time this gets executed
            // Otherwise the it would get miraculous speed
            // e.g.: 0 finished --> 500 finished, when instead actually 460 finished --> 500 finished
            // but wasn't tracked anymore
            if(lastFinished != 0 || lastTime != 0){
                // Multiply by 1.0 to prevent implicit integer conversion
                speedSum += ((c.getFinished() - lastFinished) / (c.getTime() - lastTime * 1.0));
                avgElements++;
            }
            lastFinished = c.getFinished();
            lastTime = c.getTime();
        }
        return speedSum / avgElements;
    }

    /**
     * Calculates an estimated time of arrival(/finish) using the previous processing speeds as defined by relevantCycles
     * @return  Estimated time of arrival/finish
     */
    public LocalDateTime getETA(){
        double averageSpeed = getAverageSpeed();
        if(averageSpeed > 0){
            return LocalDateTime.now().withFieldAdded(
                    DurationFieldType.millis(),
                    (int)(measurementList.get(measurementList.size()-1).getPending() / averageSpeed));
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
