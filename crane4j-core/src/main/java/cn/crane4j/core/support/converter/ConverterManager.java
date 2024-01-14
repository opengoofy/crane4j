package cn.crane4j.core.support.converter;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A manager for converter what convert target type to result type.
 *
 * @author huangchengxing
 * @see ParameterConvertibleMethodInvoker
 * @see HutoolConverterManager
 * @see SimpleConverterManager
 * @since 1.3.0
 */
public interface ConverterManager {

    /**
     * Get converter from target type to result type.<br />
     * eg:
     * <pre>{@code
     *   ConverterManager converterManager = new SimpleConverterManager();
     *   // get converter from String to Integer
     *   BiFunction<String, Integer, Integer> converter = converterManager.getConverter(String.class, Integer.class);
     *   // convert target to Integer, if target is null or target can't convert to Integer, return 0
     *   Object target = "1";
     *   Integer result = converter.apply(target, 0);
     * }</pre>
     *
     * @param targetType target type
     * @param resultType result type
     * @param <T>        target type
     * @param <R>        result type
     * @return converter
     */
    @Nullable
    <T, R> BiFunction<T, R, R> getConverter(Class<T> targetType, Class<R> resultType);

    /**
     * Convert target to result type through converter, if converter is null, return default result value.
     *
     * @param target     target object
     * @param resultType result type
     * @param defaultResult default result value
     * @param <T>        target type
     * @param <R>        result type
     * @return converted object, if converter is null, return default value
     * @see #getConverter
     */
    @SuppressWarnings("unchecked")
    default <T, R> R convert(T target, Class<R> resultType, R defaultResult) {
        if (Objects.isNull(target)) {
            return defaultResult;
        }
        if (resultType.isInstance(target)) {
            return (R) target;
        }
        BiFunction<T, R, R> converter = getConverter((Class<T>) target.getClass(), resultType);
        return Objects.nonNull(converter) ? converter.apply(target, defaultResult) : defaultResult;
    }

    /**
     * Convert source to target type through converter, if converter is null, return null.
     *
     * @param target     target object
     * @param resultType result type
     * @param <T>        target type
     * @param <R>        result type
     * @return converted object, if converter is null, return null
     * @see #getConverter
     */
    default <T, R> R convert(T target, Class<R> resultType) {
        return convert(target, resultType, null);
    }
}
