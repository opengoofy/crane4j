package cn.crane4j.core.support;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * An global sorter.
 *
 * @author huangchengxing
 */
@SuppressWarnings("all")
public final class Crane4jGlobalSorter implements Comparator<Object> {

    /**
     * Default global singleton.
     */
    public static final Crane4jGlobalSorter INSTANCE = new Crane4jGlobalSorter();

    /**
     * Get comparator comparator.
     *
     * @return comparator
     */
    public static <T> Comparator<T> comparator() {
        return (Comparator<T>) INSTANCE;
    }

    /**
     * Comparator chain.
     */
    public List<Function<Object, Integer>> keyExtractors = new ArrayList<>();

    static {
        INSTANCE.addCompareValueExtractor(t -> (t instanceof Sorted) ? ((Sorted)t).getSort() : null);
    }

    /**
     * Add comparator.
     *
     * @param compareValueExtractor comparator
     */
    public synchronized void addCompareValueExtractor(Function<Object, Integer> compareValueExtractor) {
        Objects.requireNonNull(compareValueExtractor, "compareValueExtractor must not null");
        keyExtractors.remove(compareValueExtractor);
        keyExtractors.add(compareValueExtractor);
    }

    /**
     * Compare by comparator chain.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(Object o1, Object o2) {
        return getAggregatedComparator(Integer.MAX_VALUE).compare(o1, o2);
    }

    /**
     * Get sort value.
     *
     * @param target target
     * @param defVal default value if cannot extract sort value from target
     * @return sort value
     */
    public int getSortValue(Object target, int defVal) {
        return getAggregatedKeyExtractor(defVal).apply(target);
    }

    private Comparator<Object> getAggregatedComparator(int defVal) {
        return Comparator.comparingInt(t -> getSortValue(t, defVal));
    }

    private Function<Object, Integer> getAggregatedKeyExtractor(int defVal) {
        return t -> keyExtractors.stream()
            .map(extractor -> extractor.apply(t))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(defVal);
    }
}
