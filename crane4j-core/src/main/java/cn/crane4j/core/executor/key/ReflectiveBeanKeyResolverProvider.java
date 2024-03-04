package cn.crane4j.core.executor.key;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;

/**
 * <p>Key resolver, which is used to resolve the key of the operation.
 *
 * @author huangchengxing
 * @since 2.7.0
 */
@RequiredArgsConstructor
public class ReflectiveBeanKeyResolverProvider implements KeyResolverProvider {

    public static final String PROPERTY_NAME_SEPARATOR = ",";
    public static final String PROPERTY_NAME_MAPPER = ":";

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
        String[][] propertyMappings = StringUtils.isEmpty(keyDescription) ?
            resolvePropertyMappings(beanType) : resolvePropertyMappings(keyDescription);
        return new ReflectiveBeanKeyResolver(propertyMappings);
    }

    /**
     * Resolve the property mappings when no key description is specified.
     *
     * @param targetType target type
     * @return property mappings
     */
    protected String[][] resolvePropertyMappings(Class<?> targetType) {
        Field[] fields = ReflectUtils.getFields(targetType);
        String[][] results = new String[fields.length][];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            results[i] = new String[]{field.getName(), field.getName()};
        }
        return results;
    }

    /**
     * Resolve the property mappings when key description is specified.
     *
     * @param keyDescription key description
     * @return property mappings
     */
    protected String[][] resolvePropertyMappings(String keyDescription) {
        String[] mappings = keyDescription.split(PROPERTY_NAME_SEPARATOR);
        String[][] results = new String[mappings.length][];
        for (int i = 0; i < mappings.length; i++) {
            String[] mapping = mappings[i].trim()
                .split(PROPERTY_NAME_MAPPER);
            Asserts.isFalse(mapping.length > 2, "The key description is illegal: {}", keyDescription);
            results[i] = mapping.length == 2 ?
                mapping : new String[]{mapping[0].trim(), mapping[0].trim()};
        }
        return results;
    }

    @NonNull
    private static Object newInstance(Class<?> beanType) {
        return ClassUtils.newInstance(beanType);
    }

    @RequiredArgsConstructor
    private class ReflectiveBeanKeyResolver implements KeyResolver {

        private final String[][] propertyMappings;

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
            for (String[] mapping : propertyMappings) {
                String source = mapping[0];
                Object propertyValue = propertyOperator.readProperty(targetType, target, source);
                String reference = mapping[1];
                propertyOperator.writeProperty(beanType, bean, reference, propertyValue);
            }
            return bean;
        }
    }
}
