package cn.crane4j.core.util;

import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

/**
 * A lazy initialization holder class.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
@RequiredArgsConstructor
public class Lazy<T> implements Supplier<T> {

    private volatile T value;
    private final Supplier<T> supplier;

    /**
     * Initializes the value if it hasn't been already then returns it.
     *
     * @return the value
     */
    public T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = supplier.get();
                }
            }
        }
        return value;
    }

    public synchronized T refresh() {
        T oldValue = this.value;
        this.value = null;
        return oldValue;
    }
}
