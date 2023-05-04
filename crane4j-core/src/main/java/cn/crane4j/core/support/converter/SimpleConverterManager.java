package cn.crane4j.core.support.converter;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * A simple implementation of {@link ConverterManager}.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
public class SimpleConverterManager implements ConverterManager {

    /**
     * Get converter from target type to result type.
     *
     * @param targetType target type
     * @param resultType result type
     * @return converter
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <S, R> BiFunction<S, R, R> getConverter(Class<S> targetType, Class<R> resultType) {
        return (t, d) -> {
            R result;
            try {
                result = (R)t;
            } catch (ClassCastException e) {
                result = d;
            }
            return result;
        };
    }
}
