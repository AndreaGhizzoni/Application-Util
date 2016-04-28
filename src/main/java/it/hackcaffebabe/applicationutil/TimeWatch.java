package it.hackcaffebabe.applicationutil;

import java.util.concurrent.TimeUnit;

/**
 * TimeWatch class check the execution time of code.
 * <pre>{@code
 *      TimeWatch w = TimeWatch.start();
 *      // ...very long operation...
 *      long elapsed = w.stop();
 *      System.out.printf("Elapsed time: %l", elapsed);
 * }</pre>
 *
 * To reset an instance of TimeWatch:
 * <pre>{@code
 *      //[previous example]
 *
 *      w = w.start(); //obtain new instance
 *      // ...another very long operation...
 *      elapsed = w.stop();
 *      System.out.printf("Elapsed time: %l", elapsed);
 * }</pre>
 */
public class TimeWatch
{
    long starts, elapsed;

    /* TimeWatch constructor */
    private TimeWatch(){
        starts = System.nanoTime();
        elapsed = 0;
    }

//==============================================================================
//  METHOD
//==============================================================================
    /**
     * This static method instance a the TimeWatch and starts it.
     * @return {@link it.hackcaffebabe.applicationutil.TimeWatch} the current
     *         instance of TimeWatch
     */
    public static TimeWatch start() {
        return new TimeWatch();
    }

    /**
     * Stop the timer and return the elapsed time passed since calling start()
     * method in nanoseconds.
     * @return {@link java.lang.Long} the number of nanoseconds passed.
     */
    public long stop() {
        if( elapsed == 0 )
            elapsed = System.nanoTime() - starts;
        return elapsed;
    }

    /**
     * Static method that convert the time passed as first argument into the
     * time unit passed as second argument. Time must be expressed in nano
     * seconds.
     * @param time {@link java.lang.Long} time in nanoseconds
     * @param unit {@link java.util.concurrent.TimeUnit} time unit to convert
     * @return {@link java.lang.Long} the time converted
     */
    public static long convert(long time, TimeUnit unit ){
        return unit.convert(time, TimeUnit.NANOSECONDS);
    }

//==============================================================================
//  GETTER
//==============================================================================
    /** @return {@link java.lang.Long} the time in nanoseconds when start()
     *          method has called */
    public long getStartingTime(){ return this.starts; }

    /** @return {@link java.lang.Long} the time elapsed between the calling of
     *          start() and stop() method */
    public long getElapsedTime(){ return this.elapsed; }
}
