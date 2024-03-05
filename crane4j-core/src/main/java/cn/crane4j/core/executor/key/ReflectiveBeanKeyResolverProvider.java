package cn.crane4j.core.executor.key;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Key resolver, which is used to resolve the key of the operation.
 *
 * @author huangchengxing
 * @since 2.7.0
 */
@RequiredArgsConstructor
public class ReflectiveBeanKeyResolverProvider implements KeyResolverProvider {

    /**
     * Property operator
     */
    private final PropertyOperator propertyOperator;

    /**
     * Get the resolver of the operation.
     *
     * @param operation operation
     * @return resolver
     */
    @Override
    public KeyResolver getResolver(AssembleOperation operation) {
        Asserts.isEmpty(operation.getKey(), "Cannot specify the key for the operation from {} when using the bean key resolver", operation.getSource());
        // check bean is instantiable
        Class<?> beanType = operation.getKeyType();
        Asserts.isNotNull(beanType, "The bean type must not be null");
        Asserts.isFalse(ClassUtils.isObjectOrVoid(beanType), "The bean type must not be Object or void");
        try {
            newInstance(beanType);
        } catch (Exception e) {
            throw new Crane4jException("The bean [{}] is not instantiable, please make sure it has a no-argument constructor: ", beanType, e.getMessage());
        }
        // resolve property mappings
        String keyDescription = operation.getKeyDescription();
        Set<PropertyMapping> propertyMappings = StringUtils.isEmpty(keyDescription) ?
            resolvePropertyMappings(beanType) : resolvePropertyMappings(keyDescription);
        return new ReflectiveBeanKeyResolver(propertyMappings.toArray(new PropertyMapping[0]));
    }

    /**
     * Resolve the property mappings when no key description is specified.
     *
     * @param targetType target type
     * @return property mappings
     */
    protected Set<PropertyMapping> resolvePropertyMappings(Class<?> targetType) {
        return Arrays.stream(ReflectUtils.getFields(targetType))
            .map(field -> new SimplePropertyMapping(field.getName(), field.getName()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Resolve the property mappings when key description is specified.
     *
     * @param keyDescription key description
     * @return property mappings
     */
    protected Set<PropertyMapping> resolvePropertyMappings(String keyDescription) {
        Set<PropertyMapping> mappings = SimplePropertyMapping.from(keyDescription);
        mappings.forEach(m -> Asserts.isTrue(
            m.hasSource() && StringUtils.isNotEmpty(m.getReference()),
            "The property mappings is illegal: {} -> {}", m.getReference(), m.getSource()
        ));
        return mappings;
    }

    @NonNull
    private static Object newInstance(Class<?> beanType) {
        return ClassUtils.newInstance(beanType);
    }

    @RequiredArgsConstructor
    private class ReflectiveBeanKeyResolver implements KeyResolver {

        private final PropertyMapping[] propertyMappings;

        /**
         * Resolve the key of the operation.
         *
         * @param target    target
         * @param operation operation
         * @return key
         */
        @SuppressWarnings("all")
        @Override
        public Object resolve(Object target, AssembleOperation operation) {
            Class<?> beanType = operation.getKeyType();
            Asserts.isNotNull(beanType, "The bean type must not be null");
            Object bean = newInstance(beanType);
            // copy property values from target to bean
            Class<?> targetType = target.getClass();
            for (PropertyMapping mapping : propertyMappings) {
                Object propertyValue = propertyOperator.readProperty(targetType, target, mapping.getSource());
                propertyOperator.writeProperty(beanType, bean, mapping.getReference(), propertyValue);
            }
            return bean;
        }
    }
}
