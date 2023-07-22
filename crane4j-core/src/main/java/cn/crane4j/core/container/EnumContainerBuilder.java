package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A builder class for creating {@link Container}s from enumerations.
 *
 * @param <K> key type
 * @param <T> enumeration type
 * @author tangcent
 * @see ContainerEnum
 */
public class EnumContainerBuilder<K, T extends Enum<?>> {

    private final Class<T> enumType;
    private String namespace;
    private Function<? super T, ?> keyGetter = DEFAULT_KEY_GETTER;
    private Function<? super T, ?> valueGetter = DEFAULT_VALUE_GETTER;
    private AnnotationFinder annotationFinder;
    private PropertyOperator propertyOperator;

    private static final Function<? super Enum<?>, ?> DEFAULT_KEY_GETTER = Enum::name;
    private static final Function<? super Enum<?>, ?> DEFAULT_VALUE_GETTER = Function.identity();

    /**
     * Creates a new instance of the builder with the specified enum type.
     *
     * @param enumType the enum type to create a container for
     */
    private EnumContainerBuilder(Class<T> enumType) {
        this.enumType = enumType;
    }

    /**
     * Returns a new builder for the specified enum type.
     *
     * @param enumType the enum type to create a container for
     * @param <T>      the type of the enum values
     * @return a new builder instance
     */
    public static <T extends Enum<?>> EnumContainerBuilder<Object, T> of(Class<T> enumType) {
        return new EnumContainerBuilder<>(enumType);
    }

    /**
     * Sets the namespace for the container.
     *
     * @param namespace the namespace to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Sets the function to use for obtaining keys from enum values.
     *
     * @param keyGetter the key getter function to set
     * @param <K1>      the type of the keys returned by the key getter function
     * @return this builder instance
     */
    @SuppressWarnings("unchecked")
    public <K1> EnumContainerBuilder<K1, T> keyGetter(Function<? super T, K1> keyGetter) {
        this.keyGetter = keyGetter;
        return (EnumContainerBuilder<K1, T>) this;
    }

    /**
     * Sets the key to use for obtaining keys from enum values.
     *
     * @param key the key to set
     * @return this builder instance
     */
    @SuppressWarnings("unchecked")
    public EnumContainerBuilder<Object, T> key(String key) {
        this.keyGetter = (e) -> this.propertyOperator.readProperty(enumType, e, key);
        return (EnumContainerBuilder<Object, T>) this;
    }


    /**
     * Sets the function to use for obtaining values from enum values.
     *
     * @param valueGetter the value getter function to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> valueGetter(Function<? super T, Object> valueGetter) {
        this.valueGetter = valueGetter;
        return this;
    }

    /**
     * Sets the value to use for obtaining values from enum values.
     *
     * @param value the value to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> value(String value) {
        this.valueGetter = (e) -> this.propertyOperator.readProperty(enumType, e, value);
        return this;
    }

    /**
     * Sets the annotation finder to use for reading the {@link ContainerEnum} annotation.
     *
     * @param annotationFinder the annotation finder to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> annotationFinder(AnnotationFinder annotationFinder) {
        this.annotationFinder = annotationFinder;
        return this;
    }


    /**
     * Sets the property operator to use for reading properties from enum values.
     *
     * @param propertyOperator the property operator to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> propertyOperator(PropertyOperator propertyOperator) {
        this.propertyOperator = propertyOperator;
        return this;
    }

    /**
     * Builds and returns an immutable container from the specified enum type and builder configuration.
     *
     * @return an immutable container
     */
    @SuppressWarnings("unchecked")
    public Container<K> build() {
        Objects.requireNonNull(enumType);

        if (annotationFinder != null) {
            ContainerEnum annotation = annotationFinder.getAnnotation(enumType, ContainerEnum.class);
            if (Objects.nonNull(annotation)) {
                // enumeration is annotated
                if (namespace == null) {
                    namespace = StringUtils.emptyToDefault(annotation.namespace(), enumType.getSimpleName());
                }
                boolean hasKey = StringUtils.isNotEmpty(annotation.key());
                boolean hasValue = StringUtils.isNotEmpty(annotation.value());
                if (propertyOperator != null) {
                    if (hasKey && keyGetter == DEFAULT_KEY_GETTER) {
                        keyGetter = (e) -> propertyOperator.readProperty(enumType, e, annotation.key());
                    }
                    if (hasValue && valueGetter == DEFAULT_VALUE_GETTER) {
                        valueGetter = (e) -> propertyOperator.readProperty(enumType, e, annotation.value());
                    }
                }
            }
        }

        Map<K, T> enumMap = (Map<K, T>) (Map<?, ?>) Stream.of(enumType.getEnumConstants())
            .collect(Collectors.toMap(keyGetter, valueGetter));
        namespace = StringUtils.emptyToDefault(this.namespace, enumType.getSimpleName());

        return ImmutableMapContainer.forMap(namespace, enumMap);
    }
}