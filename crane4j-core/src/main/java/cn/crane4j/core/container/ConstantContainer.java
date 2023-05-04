package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
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
 * <p>A container that stores key-value pairs.
 *
 * <p>Supports the following factory methods to create containers:
 * <ul>
 *     <li>{@link #forMap}: key-value pairs in the specified map;</li>
 *     <li>{@link #forEnum}: enumeration type, key or value is the enumeration attribute value;</li>
 *     <li>{@link #forConstantClass}: static constants attribute in the specified class;</li>
 * </ul>
 * this method also supports configuration through annotations.
 *
 * <p>for performance reasons, when get data from container,
 * it always returns all data which set in the creation time.
 * and data will not be updated after the container is created.
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
        String namespace = StringUtils.emptyToDefault(annotation.namespace(), enumType.getSimpleName());
        boolean hasKey = StringUtils.isNotEmpty(annotation.key());
        boolean hasValue = StringUtils.isNotEmpty(annotation.value());
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
        Asserts.isNotNull(annotation, "cannot find @ContainerConstant from [{}]", constantClass);
        boolean onlyPublic = annotation.onlyPublic();
        boolean onlyExplicitlyIncluded = annotation.onlyExplicitlyIncluded();
        // get attribute
        Field[] fields = ReflectUtils.getFields(constantClass);
        Map<Object, Object> data = new LinkedHashMap<>();
        Stream.of(fields)
            .filter(field -> Modifier.isStatic(field.getModifiers()))
            .filter(field -> !onlyPublic || Modifier.isPublic(field.getModifiers()))
            .filter(field -> !onlyExplicitlyIncluded || annotationFinder.hasAnnotation(field, ContainerConstant.Include.class))
            .filter(field -> !annotationFinder.hasAnnotation(field, ContainerConstant.Exclude.class))
            .forEach(field -> {
                Object value = ReflectUtils.getFieldValue(null, field);
                ContainerConstant.Name name = annotationFinder.getAnnotation(field, ContainerConstant.Name.class);
                String key = Objects.isNull(name) ? field.getName() : name.value();
                data.put(key, value);
            });
        // build container
        String namespace = StringUtils.emptyToDefault(annotation.namespace(), constantClass.getSimpleName());
        return forMap(namespace, annotation.reverse() ? CollectionUtils.reverse(data) : data);
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
        Asserts.isNotEmpty(data, "data must not empty");
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
