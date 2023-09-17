package cn.crane4j.core.support.converter;

import cn.crane4j.core.util.ObjectUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BiFunction;

/**
 * A simple implementation of {@link ConverterManager}.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
public class SimpleConverterManager implements ConverterManager {

    public static final SimpleConverterManager INSTANCE = new SimpleConverterManager();

    /**
     * Get converter from target type to result type.
     *
     * @param targetType target type
     * @param resultType result type
     * @param <T>        target type
     * @param <R>        result type
     * @return converter
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T, R> BiFunction<T, R, R> getConverter(Class<T> targetType, Class<R> resultType) {
        return (t, d) -> ObjectUtils.defaultIfNull((R)t, d);
    }
}
