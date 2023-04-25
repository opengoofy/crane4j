package cn.crane4j.core.util;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

/**
 * Support for {@link ReadWriteLock}.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ReadWriteLockSupport {

    private final ReadWriteLock lock;

    /**
     * Do something with read lock.
     *
     * @param supplier supplier
     * @param <R>      result type
     * @return result
     */
    public <R> R withReadLock(Supplier<R> supplier) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return supplier.get();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Do something with write lock.
     *
     * @param runnable runnable
     */
    public void withWriteLock(Runnable runnable) {
        withWriteLock(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Do something with write lock.
     *
     * @param supplier supplier
     * @param <R>      result type
     * @return result
     */
    public <R> R withWriteLock(Supplier<R> supplier) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            return supplier.get();
        } finally {
            writeLock.unlock();
        }
    }
}
