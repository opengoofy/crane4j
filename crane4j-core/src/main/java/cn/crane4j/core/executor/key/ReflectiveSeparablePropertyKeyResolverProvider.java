package cn.crane4j.core.executor.key;

import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>Key resolver, which is used to resolve the key of the operation.
 *
 * @author huangchengxing
 * @since 2.7.0
 */
@RequiredArgsConstructor
public class ReflectiveSeparablePropertyKeyResolverProvider implements KeyResolverProvider {

    private static final String DEFAULT_KEY_SPLITTER = ",";
    private final PropertyOperator propertyOperator;
    private final ConverterManager converterManager;

    /**
     * Get the resolver of the operation.
     *
     * @param operation operation
     * @return resolver
     */
    @Override
    public KeyResolver getResolver(AssembleOperation operation) {
        Asserts.isNotEmpty(operation.getKey(), "No key is specified for the operation from {}", operation.getSource());
        String keySplitter = StringUtils.emptyToDefault(operation.getKeyDescription(), DEFAULT_KEY_SPLITTER);
        return new ReflectiveSeparablePropertyKeyResolver(keySplitter);
    }

    /**
     * Split the key value.
     *
     * @param propertyValue property value
     * @param keySplitter key splitter
     * @return split values
     */
    @NonNull
    @SuppressWarnings("unchecked")
    protected Collection<Object> splitKey(@Nullable Object propertyValue, String keySplitter) {
        if (Objects.isNull(propertyValue)) {
            return Collections.emptyList();
        }
        if (propertyValue instanceof CharSequence) {
            CharSequence cs = (CharSequence) propertyValue;
            if (StringUtils.isEmpty(cs)) {
                return Collections.emptyList();
            }
            String[] split = ((String)propertyValue).split(keySplitter);
            return split.length > 1 ?
                Arrays.stream(split).map(String::trim).collect(Collectors.toList()) :
                Collections.singletonList(propertyValue);
        }
        if (propertyValue instanceof Collection) {
            return (Collection<Object>) propertyValue;
        }
        if (propertyValue.getClass().isArray()) {
            return Arrays.asList((Object[]) propertyValue);
        }
        return Collections.singletonList(propertyValue);
    }

    @RequiredArgsConstructor
    private class ReflectiveSeparablePropertyKeyResolver implements KeyResolver {

        private final String keySplitter;

        @Override
        public Collection<Object> resolve(Object target, AssembleOperation operation) {
            Object propertyValue = propertyOperator.readProperty(target.getClass(), target, operation.getKey());
            Collection<Object> values = splitKey(propertyValue, keySplitter);
            if (values.isEmpty()) {
                return Collections.emptyList();
            }
            Class<?> keyType = operation.getKeyType();
            if (Objects.isNull(keyType) || ClassUtils.isObjectOrVoid(keyType)) {
                return values;
            }
            return values.stream()
                .map(v -> converterManager.convert(v, keyType))
                .collect(Collectors.toList());
        }
    }
}
