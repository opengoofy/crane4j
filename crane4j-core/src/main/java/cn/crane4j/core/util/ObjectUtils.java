package cn.crane4j.core.util;

import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author huangchengxing
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ObjectUtils {

    /**
     * <p>Get type of element from the target.<br />
     * Support {@link List}, {@link Collection}, {@link Map}, {@link Iterator}, {@link Iterable}, Array.
     *
     * @param target target
     * @return element type
     */
    public static Class<?> getElementType(Object target) {
        if (Objects.isNull(target)) {
            return null;
        }
        if (target instanceof Iterator) {
            Iterator<?> iterator = (Iterator<?>)target;
            if (!iterator.hasNext()) {
                return null;
            }
            // get first not null element
            Object curr = iterator.next();
            while (Objects.isNull(curr)) {
                if (!iterator.hasNext()) {
                    return null;
                }
                curr = iterator.next();
            }
            return curr.getClass();
        }
        if (target instanceof Collection) {
            return getElementType(((Collection<?>)target).iterator());
        }
        if (target instanceof Iterable) {
            return getElementType(((Iterable<?>)target).iterator());
        }
        if (target.getClass().isArray()) {
            return target.getClass().getComponentType();
        }
        return target.getClass();
    }

    /**
     * <p>Get target or default value if target is null.
     *
     * @param target target
     * @param defaultValue default value
     * @param <T> element type
     * @return element
     */
    public <T> T getOrDefault(T target, T defaultValue) {
        return Objects.isNull(target) ? defaultValue : target;
    }

    /**
     * <p>Get target then apply function, or default value if target is null.
     *
     * @param target target
     * @param function function
     * @param defaultValue default value
     * @param <R> return type
     * @param <T> element type
     * @return element
     */
    public <T, R> R getOrDefault(T target, Function<T, R> function, R defaultValue) {
        return Objects.isNull(target) ? defaultValue : function.apply(target);
    }

    /**
     * <p>Get a specified index element from the target.<br />
     * Support {@link List}, {@link Collection}, {@link Map}, {@link Iterator}, {@link Iterable}, {@link Object[]}.
     *
     * @param target target
     * @param <T> element type
     * @return element
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T get(Object target, int index) {
        if (Objects.isNull(target)) {
            return null;
        }
        if (target instanceof List) {
            return ((List<T>)target).get(index);
        }
        if (target instanceof Collection) {
            return ((Collection<T>)target).stream().skip(index).findFirst().orElse(null);
        }
        if (target instanceof Map) {
            return ((Map<?, T>)target).values().stream().skip(index).findFirst().orElse(null);
        }
        if (target instanceof Iterator) {
            Iterator<T> iterator = (Iterator<T>)target;
            for (int i = 0; i < index; i++) {
                if (!iterator.hasNext()) {
                    return null;
                }
                iterator.next();
            }
            return iterator.next();
        }
        if (target instanceof Iterable) {
            return get(((Iterable<T>)target).iterator(), index);
        }
        if (target.getClass().isArray()) {
            return ((T[])target)[index];
        }
        return null;
    }

    /**
     * <p>Determine whether the target is empty.<br />
     * Support {@link Map}, {@link Collection}, {@link Iterator}, {@link Iterable}, Array, {@link CharSequence}
     *
     * @param target target
     * @return boolean
     */
    public static boolean isEmpty(Object target) {
        if (Objects.isNull(target)) {
            return true;
        }
        if (target instanceof Map) {
            return ((Map<?, ?>)target).isEmpty();
        }
        if (target instanceof Collection) {
            return ((Collection<?>)target).isEmpty();
        }
        if (target instanceof Iterator) {
            return !((Iterator<?>)target).hasNext();
        }
        if (target instanceof Iterable) {
            return !((Iterable<?>)target).iterator().hasNext();
        }
        if (target.getClass().isArray()) {
            return Array.getLength(target) == 0;
        }
        if (target instanceof CharSequence) {
            return ((CharSequence)target).length() == 0;
        }
        return false;
    }

    /**
     * Determine whether the target is not empty.
     *
     * @param target target
     * @return boolean
     */
    public static boolean isNotEmpty(Object target) {
        return !isEmpty(target);
    }
}
