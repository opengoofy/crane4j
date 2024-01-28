package cn.crane4j.core.util;

import com.google.common.collect.MapMaker;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * {@link Collection}、{@link Map}、{@link Iterator}、{@link Iterable} utils.
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtils {

    /**
     * <p>Split the given list into sub lists.
     *
     * @param list list
     * @param size size of sub list
     * @param <T> element type
     * @return sub lists
     */
    public static <T> List<Collection<T>> split(Collection<T> list, int size) {
        return split(list, size, ls -> ls);
    }

    /**
     * <p>Split the given list into sub lists.
     *
     * @param list list
     * @param size size of sub list
     * @param mapper mapper to convert list to sub list
     * @param <T> element type
     * @param <C> collection type
     * @return sub lists
     */
    public static <T, C extends Collection<T>> List<C> split(
        Collection<T> list, int size, Function<List<T>, C> mapper) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        int listSize;
        if (Objects.isNull(list) || (listSize = list.size()) == 0) {
            return Collections.emptyList();
        }
        if (listSize == size) {
            return Collections.singletonList(mapper.apply(new ArrayList<>(list)));
        }
        List<C> result = new ArrayList<>();
        List<T> subList = new ArrayList<>(size);
        for (T t : list) {
            subList.add(t);
            if (subList.size() == size) {
                result.add(mapper.apply(subList));
                subList = new ArrayList<>(size);
            }
        }
        if (!subList.isEmpty()) {
            result.add(mapper.apply(subList));
        }
        return result;
    }

    /**
     * <p>Get first not null element from the target.<br />
     *
     * @param iterator iterator
     * @param <T> element type
     * @return first not null element
     */
    public static <T> T getFirstNotNull(Iterator<T> iterator) {
        if (Objects.isNull(iterator)) {
            return null;
        }
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (Objects.nonNull(t)) {
                return t;
            }
        }
        return null;
    }

    /**
     * <p>Get first not null element from the target.<br />
     *
     * @param iterable iterable
     * @param <T> element type
     * @return first not null element
     */
    public static <T> T getFirstNotNull(Iterable<T> iterable) {
        if (Objects.isNull(iterable)) {
            return null;
        }
        return getFirstNotNull(iterable.iterator());
    }

    /**
     * <p>Reverse given map.
     *
     * @param map map to reverse
     * @return reversed map
     */
    public static <K, V> Map<V, K> reverse(Map<K, V> map) {
        if (isEmpty(map)) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * <p>Get collection if not null, otherwise return default collection.
     *
     * @param collection collection
     * @param defaultCollection default collection
     * @param <T> element type
     * @param <C> collection type
     * @return collection
     */
    public static <T, C extends Collection<T>> C defaultIfEmpty(C collection, C defaultCollection) {
        return isEmpty(collection) ? defaultCollection : collection;
    }

    /**
     * <p>Check whether coll1 contains any element of coll2.
     *
     * @param coll1 coll1
     * @param coll2 coll2
     * @return true if coll1 contains any element of coll2, otherwise false
     */
    public static boolean containsAny(Collection<?> coll1, Collection<?> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return false;
        }
        // size of coll1 less than coll2
        if (coll1.size() < coll2.size()) {
            for (Object obj : coll1) {
                if (coll2.contains(obj)) {
                    return true;
                }
            }
            return false;
        }
        // size of coll1 greater than or equal to coll2
        for (Object obj : coll2) {
            if (coll1.contains(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Check whether coll1 not contains any element of coll2.
     *
     * @param coll1 coll1
     * @param coll2 coll2
     * @return true if coll1 not contains any element of coll2, otherwise false
     */
    public static boolean notContainsAny(Collection<?> coll1, Collection<?> coll2) {
        return !containsAny(coll1, coll2);
    }

    /**
     * <p>Get value specified index from {@link Collection}.
     *
     * @param collection collection
     * @param index index, if index less than 0 or greater than or equal to collection size, return null
     * @param <T> element type
     * @return value
     */
    public static <T> T get(Collection<T> collection, int index) {
        if (Objects.isNull(collection)) {
            return null;
        }
        if (collection instanceof List) {
            // check bounds
            if (index < 0 || index >= collection.size()) {
                return null;
            }
            return ((List<T>)collection).get(index);
        }
        return get(collection.iterator(), index);
    }

    /**
     * <p>Get value specified index from {@link Iterable}.
     *
     * @param iterable iterable
     * @param index index
     * @param <T> element type
     * @return value
     */
    public static <T> T get(Iterable<T> iterable, int index) {
        if (Objects.isNull(iterable)) {
            return null;
        }
        return get(iterable.iterator(), index);
    }

    /**
     * <p>Get value specified index from {@link Iterable}.
     *
     * @param iterable iterable
     * @param index index
     * @param <T> element type
     * @return value
     */
    public static <T> T get(Iterator<T> iterable, int index) {
        if (Objects.isNull(iterable)) {
            return null;
        }
        int i = 0;
        while (iterable.hasNext()) {
            T next = iterable.next();
            if (i == index) {
                return next;
            }
            i++;
        }
        return null;
    }

    /**
     * <p>Add all elements to the collection.
     *
     * @param collection collection
     * @param elements elements
     * @param <T> element type
     * @param <C> collection type
     * @return {@link Collection} itself if not null, or empty collection if null
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T, C extends Collection<T>> C addAll(C collection, T... elements) {
        if (Objects.isNull(collection)) {
            return (C)Collections.emptyList();
        }
        if (Objects.isNull(elements) || elements.length == 0) {
            return collection;
        }
        collection.addAll(Arrays.asList(elements));
        return collection;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T, C extends Collection<T>> C addAll(C collection, Collection<T> elements) {
        if (Objects.isNull(collection)) {
            return (C)Collections.emptyList();
        }
        if (Objects.isNull(elements) || elements.isEmpty()) {
            return collection;
        }
        collection.addAll(elements);
        return collection;
    }

    /**
     * <p>Create a collection from given elements.
     *
     * @param collectionFactory collection factory
     * @param elements elements
     * @param <T> element type
     * @param <C> collection type
     * @return collection
     */
    @SafeVarargs
    public static <T, C extends Collection<T>> C newCollection(Supplier<C> collectionFactory, T... elements) {
        C collection = collectionFactory.get();
        Objects.requireNonNull(collection, "the collection obtained from the collection factory cannot be null");
        if (Objects.isNull(elements) || elements.length == 0) {
            return collection;
        }
        collection.addAll(Arrays.asList(elements));
        return collection;
    }

    /**
     * <p>Whether the collection is empty.
     *
     * @param collection collection
     * @return true if empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * <p>Whether the collection is not empty.
     *
     * @param collection collection
     * @return true if not empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * <p>Whether the map is empty.
     *
     * @param map map
     * @return true if empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * <p>Whether the map is not empty.
     *
     * @param map collection
     * @return true if not empty
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * <p>Whether the iterator is empty.
     *
     * @param iterator iterator
     * @return true if empty
     */
    public static boolean isEmpty(Iterator<?> iterator) {
        return iterator == null || !iterator.hasNext();
    }

    /**
     * <p>Whether the iterator is not empty.
     *
     * @param iterator iterator
     * @return true if not empty
     */
    public static boolean isNotEmpty(Iterator<?> iterator) {
        return !isEmpty(iterator);
    }

    /**
     * <p>Whether the iterable is empty.
     *
     * @param iterable iterable
     * @return true if empty
     */
    public static boolean isEmpty(Iterable<?> iterable) {
        return iterable == null || isEmpty(iterable.iterator());
    }

    /**
     * <p>Whether the iterable is not empty.
     *
     * @param iterable iterable
     * @return true if not empty
     */
    public static boolean isNotEmpty(Iterable<?> iterable) {
        return !isEmpty(iterable);
    }

    /**
     * Create a thread-safe weak reference collection.
     *
     * @return {@link ConcurrentMap}
     */
    public static <K, V> ConcurrentMap<K, V> newWeakConcurrentMap() {
        return new MapMaker()
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .weakKeys().weakValues()
            .makeMap();
    }

    /**
     * Adapt a {@link Object} object to {@link Collection}.
     *
     * @param obj obj
     * @return collection
     */
    public static Collection<?> adaptObjectToCollection(Object obj) {
        if (Objects.isNull(obj)) {
            return Collections.emptyList();
        }
        if (obj instanceof Collection) {
            return (Collection<?>)obj;
        }
        if (obj.getClass().isArray()) {
            return Arrays.asList((Object[])obj);
        }
        if (obj instanceof Iterable) {
            List<Object> results = CollectionUtils.newCollection(ArrayList::new);
            ((Iterable<?>)obj).forEach(results::add);
            return results;
        }
        if (obj instanceof Iterator) {
            List<Object> results = CollectionUtils.newCollection(ArrayList::new);
            ((Iterator<?>)obj).forEachRemaining(results::add);
            return results;
        }
        return Collections.singletonList(obj);
    }


    /**
     * A temporary workaround for Java 8 specific performance issue JDK-8161372 .<br>
     * This class should be removed once we drop Java 8 support.
     *
     * @param map map
     * @param key key
     * @param mappingFunction mapping function
     * @param <K> key type
     * @param <V> value type
     * @return value
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">https://bugs.openjdk.java.net/browse/JDK-8161372</a>
     */
    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<? super K, ? extends V> mappingFunction) {
        V value = map.get(key);
        if (null == value) {
            map.putIfAbsent(key, mappingFunction.apply(key));
            value = map.get(key);
        }
        return value;
    }
}
