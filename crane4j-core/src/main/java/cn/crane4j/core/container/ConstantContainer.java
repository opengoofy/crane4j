package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A simple key-value pair constant data source container is used to support
 * data sources of constant enumeration type.
 * It allows rapid construction of data sources based on specific scenarios
 * through the built-in {@code forXXX} method.
 *
 * <p>For performance reasons, the {@link #get} method
 * always obtains the full amount of data from the container,
 * rather than only corresponding to the entered key.
 *
 * @author huangchengxing
 * @param <K> key type
 * @see ContainerEnum
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantContainer<K> implements Container<K> {

    /**
     * namespace of the data source container,
     */
    @Getter
    private final String namespace;

    /**
     * data source objects grouped by key value
     */
    private final Map<K, ?> data;

    /**
     * <p>Create a key-value pair container based on the specified type enumeration.<br />
     * The key value is the enumeration attribute value obtained by {@code keyGetter}.
     *
     * @param namespace namespace
     * @param enumType enum type
     * @param keyGetter method to obtain the corresponding key value from the enumeration instance
     * @param <K> key type
     * @param <T> enumeration type
     * @return container
     */
    @Nonnull
    public static <K, T extends Enum<?>> ConstantContainer<K> forEnum(
        String namespace, Class<T> enumType, Function<? super T, ? extends K> keyGetter) {
        Objects.requireNonNull(enumType);
        Objects.requireNonNull(keyGetter);
        Map<K, T> enumMap = Stream.of(enumType.getEnumConstants())
            .collect(Collectors.toMap(keyGetter, Function.identity()));
        return forMap(namespace, enumMap);
    }

    /**
     * <p>Create a key-value pair container based on the specified type enumeration.<br />
     * The key value and namespace are obtained through the corresponding {@link ContainerEnum} annotation
     *
     * @param enumType enum type
     * @param annotationFinder annotation finder
     * @param propertyOperator property operator
     * @return container
     * @see ContainerEnum
     */
    @SuppressWarnings("unchecked")
    public static <K> ConstantContainer<K> forEnum(
        Class<? extends Enum<?>> enumType, AnnotationFinder annotationFinder, PropertyOperator propertyOperator) {
        Objects.requireNonNull(enumType);
        Objects.requireNonNull(annotationFinder);
        // enumeration is not annotated
        ContainerEnum annotation = annotationFinder.getAnnotation(enumType, ContainerEnum.class);
        if (Objects.isNull(annotation)) {
            return (ConstantContainer<K>)forEnum(enumType.getSimpleName(), enumType, Enum::name);
        }
        String namespace = CharSequenceUtil.emptyToDefault(annotation.namespace(), enumType.getSimpleName());
        boolean hasKey = CharSequenceUtil.isNotEmpty(annotation.key());
        boolean hasValue = CharSequenceUtil.isNotEmpty(annotation.value());
        Map<K, Object> map = Stream.of(enumType.getEnumConstants()).collect(Collectors.toMap(
            e -> hasKey ? (K)Objects.requireNonNull(propertyOperator.readProperty(enumType, e, annotation.key())) : (K)e.name(),
            e -> hasValue ? Objects.requireNonNull(propertyOperator.readProperty(enumType, e, annotation.value())) : e
        ));
        return forMap(namespace, map);
    }

    /**
     * Convert public static constants in the specified constant class to containers.
     *
     * @param constantClass constant class
     * @return container
     * @see ContainerConstant
     */
    public static ConstantContainer<?> forConstantClass(
        Class<?> constantClass, AnnotationFinder annotationFinder) {
        Objects.requireNonNull(constantClass);
        ContainerConstant annotation = annotationFinder.getAnnotation(constantClass, ContainerConstant.class);
        Assert.notNull(annotation, "cannot find @ContainerConstant from [{}]", constantClass);
        boolean onlyPublic = annotation.onlyPublic();
        boolean onlyExplicitlyIncluded = annotation.onlyExplicitlyIncluded();
        // get attribute
        Field[] fields = ReflectUtil.getFields(constantClass);
        Map<Object, Object> data = new LinkedHashMap<>();
        Stream.of(fields)
            .filter(field -> Modifier.isStatic(field.getModifiers()))
            .filter(field -> !onlyPublic || Modifier.isPublic(field.getModifiers()))
            .filter(field -> !onlyExplicitlyIncluded || annotationFinder.hasAnnotation(field, ContainerConstant.Include.class))
            .filter(field -> !annotationFinder.hasAnnotation(field, ContainerConstant.Exclude.class))
            .forEach(field -> {
                Object value = ReflectUtil.getStaticFieldValue(field);
                ContainerConstant.Name name = annotationFinder.getAnnotation(field, ContainerConstant.Name.class);
                String key = Objects.isNull(name) ? field.getName() : name.value();
                data.put(key, value);
            });
        // build container
        String namespace = CharSequenceUtil.emptyToDefault(annotation.namespace(), constantClass.getSimpleName());
        return forMap(namespace, annotation.reverse() ? MapUtil.reverse(data) : data);
    }

    /**
     * <p>Create a key-value pair container based on the specified type enumeration.<br />
     * The key value is the enumeration attribute value obtained by {@code keyGetter}.
     *
     * @param namespace namespace
     * @param data data source objects grouped by key value
     * @param <K> key type
     * @return container
     */
    public static <K> ConstantContainer<K> forMap(String namespace, Map<K, ?> data) {
        Objects.requireNonNull(namespace);
        Assert.notEmpty(data, "data must not empty");
        return new ConstantContainer<>(namespace, data);
    }

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @Override
    public Map<K, ?> get(Collection<K> keys) {
        return data;
    }
}
