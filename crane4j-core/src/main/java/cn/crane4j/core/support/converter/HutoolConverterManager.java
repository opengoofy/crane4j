package cn.crane4j.core.support.converter;

import cn.hutool.core.convert.Convert;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * A {@link ConverterManager} implementation based on Hutool {@link Convert}.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
public class HutoolConverterManager implements ConverterManager {

    private static final HutoolConverterManager INSTANCE = new HutoolConverterManager();

    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static HutoolConverterManager getInstance() {
        return INSTANCE;
    }

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
        return (source, defaultValue) -> {
            if (resultType.isInstance(source)) {
                return (R)source;
            }
            return Convert.convert(resultType, source, defaultValue);
        };
    }
}
