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

    private List<Calculation> calculationList;

    public ETACalculator(final int relevantCycles){
        this.RELEVANT_CYCLES = relevantCycles;
        calculationList = new LinkedList<>();
    }

    /**
     * Calculates the average speed over all submitted points
     * @return  Average speed over all submitted points
     */
    public double getAverageSpeed(){
        double avgSpeed = 0;
        int lastFinished = 0;
        long lastTime = 0;

        for (Calculation c : calculationList) {
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
        if(calculationList.size() == RELEVANT_CYCLES){
            avgSpeed /= (calculationList.size()-1);
        } else {
            avgSpeed /= calculationList.size()-2;
        }
        return avgSpeed;
    }

    /**
     * Calculates an estimated time of arrival(/finish) using the previous processing speeds as defined by relevantCycles
     * @return  Estimated time of arrival/finish
     */
    public LocalDateTime getETA(){
        if(calculationList.size() > 0){
            return LocalDateTime.now().withFieldAdded(
                    DurationFieldType.millis(),
                    (int)(calculationList.get(calculationList.size()-1).getPending() / getAverageSpeed()));
        }
        return null;
    }

    /**
     * Adds a point to the calculator which it uses to later calculate the average
     * @param started       Number of started Jobs
     * @param finished      Number of finished Jobs
     * @param currentTimeInMillis   Current time corresponding to the other parameters
     */
    public void addCycle(int started, int finished, long currentTimeInMillis){
        Calculation calculation = new Calculation(started, finished, currentTimeInMillis);
        if(calculationList.size() == RELEVANT_CYCLES){
            calculationList.add(calculation);
            calculationList.remove(0);
        } else {
            calculationList.add(calculation);
        }
    }

    class Calculation {
        final int started;
        final int finished;
        final long time;

        Calculation(int started, int finished, long time) {
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
