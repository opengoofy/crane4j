package cn.crane4j.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.LongConsumer;
import java.util.function.Supplier;

/**
 * A util class for recording the time of execution.
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimerUtil {

    /**
     * Get the time of execution.
     *
     * @param condition condition
     * @param timeConsumer consumer of time
     * @param runnable runnable
     */
    public static void getExecutionTime(
        boolean condition, LongConsumer timeConsumer, Runnable runnable) {
        getExecutionTime(condition, timeConsumer, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Get the time of execution.
     *
     * @param condition condition
     * @param timeConsumer consumer of time
     * @param supplier supplier
     * @param <R> type of result
     * @return result
     */
    public static <R> R getExecutionTime(
        boolean condition, LongConsumer timeConsumer, Supplier<R> supplier) {
        if (!condition) {
            return supplier.get();
        }
        long start = System.currentTimeMillis();
        R result = supplier.get();
        long totalTime = System.currentTimeMillis() - start;
        timeConsumer.accept(totalTime);
        return result;
    }
}
