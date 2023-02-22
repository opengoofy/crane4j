package cn.crane4j.core.support;

/**
 * Represents an object that allows sorting from small to large according to the sorting value.
 *
 * @author huangchengxing
 */
public interface Sorted {

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
