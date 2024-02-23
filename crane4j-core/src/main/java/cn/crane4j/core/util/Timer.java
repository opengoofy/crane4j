package cn.crane4j.core.util;

import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

/**
 * A util class for recording the time of execution.
 *
 * @author huangchengxing
 * @since 2.7.0
 */
public interface Timer {

    /**
     * Get a timer that can only be used once.
     *
     * @param condition enable
     * @return a timer that can only be used once
     */
    static Timer getSingeTimer(boolean condition) {
        return condition ? new SingleTimer() : NoneTimer.INSTANCE;
    }

    /**
     * Get a timer that can only be used once.
     *
     * @param condition enable
     * @return a timer that can only be used once
     */
    static Timer startTimer(boolean condition) {
        Timer timer = getSingeTimer(condition);
        timer.start();
        return timer;
    }

    /**
     * Start to watch the time.
     */
    void start();

    /**
     * Stop to watch the time and return the time in {@code timeUnit}.
     *
     * @return the time in {@code timeUnit}, if the timer is not started or stopped, return -1
     */
    long stop(TimeUnit timeUnit);

    /**
     * Run the task and consume the time in {@code timeUnit}.
     *
     * @param task task
     * @param timeUnit time unit
     * @param timeConsumer consumer of time
     */
    default void run(Runnable task, TimeUnit timeUnit, LongConsumer timeConsumer) {
        start();
        task.run();
        stop(timeUnit, timeConsumer);
    }

    /**
     * Stop to watch the time and consume the time in {@code timeUnit}.
     *
     * @param timeUnit time unit
     * @param timeConsumer consumer of time
     */
    default void stop(TimeUnit timeUnit, LongConsumer timeConsumer) {
        long time = stop(timeUnit);
        if (time > -1) {
            timeConsumer.accept(time);
        }
    }

    /**
     * A timer that can only record one time.
     */
    class SingleTimer implements Timer {

        private long start = -1;

        /**
         * Start to watch the time.
         */
        @Override
        public void start() {
            start = System.currentTimeMillis();
        }

        /**
         * Stop to watch the time.
         *
         * @param timeUnit time unit
         * @return the time in {@code timeUnit}
         */
        @Override
        public long stop(TimeUnit timeUnit) {
            long end = System.currentTimeMillis();
            long time = end - start;
            return timeUnit.convert(time, TimeUnit.MILLISECONDS);
        }
    }

    class NoneTimer implements Timer {

        public static final NoneTimer INSTANCE = new NoneTimer();

        /**
         * Start to watch the time.
         */
        @Override
        public void start() {
            // do nothing
        }

        /**
         * Stop to watch the time.
         *
         * @param timeUnit time unit
         * @return the time in {@code timeUnit}
         */
        @Override
        public long stop(TimeUnit timeUnit) {
            return -1;
        }
    }
}
