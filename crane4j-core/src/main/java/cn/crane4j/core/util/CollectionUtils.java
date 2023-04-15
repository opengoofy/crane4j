package cn.crane4j.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * CollectionUtils
 *
 * @author huangchengxing
 */
public class CollectionUtils {

    private CollectionUtils() {
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
            return Lists.newArrayList((Iterable<?>)obj);
        }
        if (obj instanceof Iterator) {
            return Lists.newArrayList((Iterator<?>)obj);
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
