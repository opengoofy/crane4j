package cn.crane4j.core.executor.key;

import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * <p>Key resolver, which is used to get key value from specified property.
 *
 * @author huangchengxing
 * @since 2.7.0
 */
@RequiredArgsConstructor
public class ReflectivePropertyKeyResolverProvider implements KeyResolverProvider {

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
        KeyResolver keyResolver = new ReflectivePropertyKeyResolver();
        Class<?> keyType = operation.getKeyType();
        if (Objects.isNull(keyType) || ClassUtils.isObjectOrVoid(keyType)) {
            return keyResolver;
        }
        return (t, op) -> {
            Object key = keyResolver.resolve(t, op);
            return converterManager.convert(key, keyType);
        };
    }

    @RequiredArgsConstructor
    public class ReflectivePropertyKeyResolver implements KeyResolver {

        /**
         * Resolve the key of the operation.
         *
         * @param target    target
         * @param operation operation
         * @return key
         */
        @Override
        public Object resolve(Object target, AssembleOperation operation) {
            return propertyOperator.readProperty(target.getClass(), target, operation.getKey());
        }
    }
}
