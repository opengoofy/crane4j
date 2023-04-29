package cn.crane4j.core.util;

import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Array utils.
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ArrayUtils {

    /**
     * <p>Append elements to array and return a new array.
     *
     * @param array array to append
     * @param elements elements
     * @param <T> element type
     * @return new array with elements appended, if array is null, return {@code elements} directly
     */
    @SafeVarargs
    public static <T> T[] append(T[] array, T... elements) {
        if (Objects.isNull(array)) {
            return elements;
        }
        if (isEmpty(elements)) {
            return Arrays.copyOf(array, array.length);
        }
        T[] result = Arrays.copyOf(array, array.length + elements.length);
        System.arraycopy(elements, 0, result, array.length, elements.length);
        return result;
    }

    /**
     * <p>Whether array is null or empty.
     *
     * @param array array
     * @return whether array is null or empty
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Join array to string with delimiter.
     *
     * @param array array
     * @param mapper mapper, map element to string
     * @param delimiter delimiter
     * @param <T> element type
     * @return string joined with delimiter, empty string if array is null
     */
    public static <T> String join(T[] array, Function<T, String> mapper, String delimiter) {
        if (Objects.isNull(array)) {
            return "";
        }
        return stream(array).map(mapper).collect(Collectors.joining(delimiter));
    }

    /**
     * <p>Join array to string with delimiter.
     *
     * @param array array of string
     * @param delimiter delimiter
     * @return string joined with delimiter, empty string if array is null
     */
    public static String join(String[] array, String delimiter) {
        return join(array, Function.identity(), delimiter);
    }

    /**
     * <p>Whether {@code target} is contained in {@code array}.
     *
     * @param array array, null returns false
     * @param target target
     * @param <T> type of array
     * @return whether {@code target} is contained in {@code array}
     */
    public static <T> boolean contains(T[] array, T target) {
        if (Objects.isNull(array)) {
            return false;
        }
        return Arrays.asList(array).contains(target);
    }

    /**
     * <p>Get stream of array, return empty stream if array is null.
     *
     * @param array array
     * @param <T> type of array
     * @return stream of array
     */
    public static <T> Stream<T> stream(T[] array) {
        if (Objects.isNull(array)) {
            return Stream.empty();
        }
        return Stream.of(array);
    }

    /**
     * <p>Get element from array, return null if array is null or index out of bounds.
     *
     * @param array array
     * @param index index
     * @param <T> type of array
     * @return element
     */
    public static <T> T get(T[] array, int index) {
        if (Objects.isNull(array)) {
            return null;
        }
        if (index < 0 || index >= array.length) {
            return null;
        }
        return array[index];
    }

    /**
     * <p>Get length of array, return 0 if array is null.
     *
     * @param array array
     * @param <T> type of array
     * @return length of array
     */
    public static <T> int length(T[] array) {
        if (Objects.isNull(array)) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Compare two arrays and return true if they are equal by {@link Objects#equals(Object, Object)}.
     *
     * @param array1 array1
     * @param array2 array2
     * @param <T> type of array1
     * @param <U> type of array2
     * @return true if they are equal
     */
    public static <T, U> boolean isEquals(T[] array1, U[] array2) {
        return isEquals(array1, array2, Objects::equals);
    }

    /**
     * <p>Compare two arrays and return true if they are equal by {@code predicate}.
     *
     * @param array1 array1
     * @param array2 array2
     * @param predicate predicate to compare
     * @param <T> type of array1
     * @param <U> type of array2
     * @return true if they are equal
     */
    public static <T, U> boolean isEquals(T[] array1, U[] array2, BiPredicate<T, U> predicate) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null) {
            return false;
        }
        int length = array1.length;
        if (length != array2.length) {
            return false;
        }
        predicate = Objects.requireNonNull(predicate, "predicate must not null").negate();
        for (int i = 0; i < length; ++i) {
            T o1 = array1[i];
            U o2 = array2[i];
            if (predicate.test(o1, o2)) {
                return false;
            }
        }
        return true;
    }
}
