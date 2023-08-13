package cn.crane4j.extension.spring;

import cn.crane4j.core.support.converter.ConverterManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.convert.ConversionService;

import java.util.function.BiFunction;

/**
 * A {@link ConverterManager} implementation based on Spring's {@link ConversionService}.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
@RequiredArgsConstructor
public class SpringConverterManager implements ConverterManager {

    /**
     * conversion service.
     */
    @Getter
    private final ConversionService conversionService;

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
     * @return converter
     */
    @Override
    public @Nullable <T, R> BiFunction<T, R, R> getConverter(Class<T> targetType, Class<R> resultType) {
        return (target, def) -> {
            try {
                return conversionService.convert(target, resultType);
            } catch (Exception e) {
                return def;
            }
        };
    }
}
