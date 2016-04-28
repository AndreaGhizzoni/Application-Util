package it.hackcaffebabe.applicationutil;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Test for class {@link TimeWatch}
 */
public class TimeWatchTest {
    @Test
    public void testCorrectBehavior(){
        long sleepingTime = 1000 * 2;

        TimeWatch w = TimeWatch.start();
        try {
            Thread.sleep(sleepingTime);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        w.stop();

        Assert.assertTrue(
                "Expect that elapsed time will be > 0",
                w.getElapsedTime() > 0
        );

        Assert.assertTrue(
                "Expect that starting time will be > 0",
                w.getStartingTime() > 0
        );

        long elapsedMilli = TimeWatch.convert(
                w.getElapsedTime(),
                TimeUnit.MILLISECONDS
        );
        Assert.assertTrue(
                "Expect that sleeping time is equal to watched time (in ms)",
                sleepingTime == elapsedMilli
        );

        long elapsedMicro = TimeWatch.convert(
                w.getElapsedTime(),
                TimeUnit.MICROSECONDS
        );
        Assert.assertTrue(
                "Expected that elapsed time in micro seconds is > than elapsed " +
                        "time in milliseconds",
                elapsedMicro >= elapsedMilli
        );
    }
}
