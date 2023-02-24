package cn.crane4j.core.support;

import java.util.Comparator;

/**
 * Represents an object that allows sorting from small to large according to the sorting value.
 *
 * @author huangchengxing
 * @see #comparator
 */
public interface Sorted {

    /**
     * Get a comparator, and the sorting rule follows {@link Sorted} semantics.
     *
     * @return comparator
     */
    static <T extends Sorted> Comparator<T> comparator() {
        return Comparator.comparing(Sorted::getSort);
    }

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    default int getSort() {
        return Integer.MAX_VALUE;
    }
}
