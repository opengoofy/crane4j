package cn.crane4j.core.util;

import com.google.common.collect.MapMaker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * CollectionUtils
 *
 * @author huangchengxing
 */
public class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * 创建一个线程安全的弱引用集合
     *
     * @return java.util.concurrent.ConcurrentMap<K,V>
     */
    public static <K, V> ConcurrentMap<K, V> newWeakConcurrentMap() {
        return new MapMaker().weakKeys().weakValues().makeMap();
    }

    /**
     * 将一个{@link Object}对象适配为{@link Collection}对象
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
        if (obj instanceof Map) {
            return ((Map<?, ?>)obj).entrySet();
        }
        return Collections.singletonList(obj);
    }

}
