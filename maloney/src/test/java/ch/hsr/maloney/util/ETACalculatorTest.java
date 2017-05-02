package ch.hsr.maloney.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by oliver on 17.04.17.
 */
public class ETACalculatorTest {
    private void linearFill(ETACalculator etaCalculator, int cycles) {
        for(int i = 0; i < cycles + 1; i++){
            etaCalculator.addMeasurement(i, i, i);
        }
    }

    private void linearETACalculator(int relevantCycles, int effectiveCycles) {
        ETACalculator etaCalculator = new ETACalculator(relevantCycles);

        linearFill(etaCalculator, effectiveCycles);

        assertEquals(1, etaCalculator.getAverageSpeed(), 0.01);
    }

    @Test
    public void getAverageSpeed() {
        linearETACalculator(5,5);
    }

    @Test
    public void fewerEntriesThanCycles() {
        linearETACalculator(5,3);
    }

    @Test
    public void extremeFewerEntriesThanCycles() {
        linearETACalculator(100,7);
    }

    @Test
    public void moreEntriesThanCycles() {
        linearETACalculator(5, 100);
    }

    @Test
    public void calculateAverageSpeed(){
        ETACalculator calculator = new ETACalculator(3);
        calculator.addMeasurement(3,0, 1000);
        calculator.addMeasurement(3,1, 2000);
        calculator.addMeasurement(3,2, 3000);
        calculator.addMeasurement(3,3, 4000);
        assertEquals(0.001, calculator.getAverageSpeed(), 0.0001);
    }

    @Test
    public void noEntries() {
        ETACalculator etaCalculator = new ETACalculator(5);
        assertEquals(0, etaCalculator.getAverageSpeed(), 0.01);
    }


}