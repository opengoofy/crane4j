package cn.crane4j.core.support;

/**
 * Represents an object that allows sorting from small to large according to the sorting value.
 *
 * @author huangchengxing
 * @see Crane4jGlobalSorter
 */
public interface Sorted extends Comparable<Sorted> {

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    default int getSort() {
        return Integer.MAX_VALUE;
    }

    /**
     * Compare the sorting value of the current object with the sorting value of the specified object.
     *
     * @param other the specified object
     * @return the result of comparing the sorting value of the current object with the sorting value of the specified object
     */
    @Override
    default int compareTo(Sorted other) {
        return Integer.compare(this.getSort(), other.getSort());
    }
}
