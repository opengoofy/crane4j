package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.annotation.DuplicateStrategy;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A builder class for creating {@link Container}s from enumerations.
 *
 * @param <K> key type
 * @param <T> enumeration type
 * @author tangcent
 * @see ContainerEnum
 */
public class EnumContainerBuilder<K, T extends Enum<?>> {

    private static final Function<? super Enum<?>, ?> DEFAULT_KEY_GETTER = Enum::name;
    private static final Function<? super Enum<?>, ?> DEFAULT_VALUE_GETTER = Function.identity();
    private static final PropertyOperator DEFAULT_PROPERTY_OPERATOR = new ReflectivePropertyOperator();

    /**
     * The enum type to create a container for.
     */
    @NonNull
    private final Class<T> enumType;

    /**
     * The namespace for the container.
     */
    @Nullable
    private String namespace;

    /**
     * The function to use for obtaining keys from enum values.
     */
    @NonNull
    private Function<? super T, ?> keyGetter = DEFAULT_KEY_GETTER;

    /**
     * The function to use for obtaining values from enum values.
     */
    @NonNull
    private Function<? super T, ?> valueGetter = DEFAULT_VALUE_GETTER;

    /**
     * Whether to enable the {@link ContainerEnum} annotation on the enum type.
     */
    private boolean enableContainerEnumAnnotation = true;

    /**
     * The annotation finder to use for finding annotations on the enum type.
     */
    @NonNull
    private AnnotationFinder annotationFinder = SimpleAnnotationFinder.INSTANCE;

    /**
     * The property operator to use for accessing properties on the enum type.
     */
    @NonNull
    private PropertyOperator propertyOperator = DEFAULT_PROPERTY_OPERATOR;

    /**
     * Processing strategy when the key is duplicated.
     *
     * @since 2.2.0
     */
    @NonNull
    private DuplicateStrategy duplicateStrategy = DuplicateStrategy.ALERT;

    /**
     * Creates a new instance of the builder with the specified enum type.
     *
     * @param enumType the enum type to create a container for
     */
    private EnumContainerBuilder(@NonNull Class<T> enumType) {
        this.enumType = Objects.requireNonNull(enumType);
    }

    /**
     * Returns a new builder for the specified enum type.
     *
     * @param enumType the enum type to create a container for
     * @param <T>      the type of the enum values
     * @return a new builder instance
     */
    public static <T extends Enum<?>> EnumContainerBuilder<Object, T> of(@NonNull Class<T> enumType) {
        return new EnumContainerBuilder<>(enumType);
    }

    // ============== attribute ==============

    /**
     * Sets the namespace for the container.
     *
     * @param namespace the namespace to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> namespace(@Nullable String namespace) {
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
    public <K1> EnumContainerBuilder<K1, T> keyGetter(@NonNull Function<? super T, K1> keyGetter) {
        this.keyGetter = keyGetter;
        return (EnumContainerBuilder<K1, T>) this;
    }

    /**
     * Sets the key to use for obtaining keys from enum values.
     *
     * @param key the key to set
     * @return this builder instance
     * @throws Crane4jException throw when cannot find key getter of given property from enum type
     */
    @SuppressWarnings("unchecked")
    public EnumContainerBuilder<Object, T> key(@NonNull String key) {
        MethodInvoker invoker = this.propertyOperator.findGetter(enumType, key);
        Asserts.isNotNull(invoker, "cannot not find getter of property [{}] from [{}]", key, enumType);
        this.keyGetter = invoker::invoke;
        return (EnumContainerBuilder<Object, T>) this;
    }


    /**
     * Sets the function to use for obtaining values from enum values.
     *
     * @param valueGetter the value getter function to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> valueGetter(@NonNull Function<? super T, Object> valueGetter) {
        this.valueGetter = valueGetter;
        return this;
    }

    /**
     * Sets the value to use for obtaining values from enum values.
     *
     * @param value the value to set
     * @return this builder instance
     * @throws Crane4jException throw when cannot find value getter of given property from enum type
     */
    public EnumContainerBuilder<K, T> value(@NonNull String value) {
        MethodInvoker invoker = this.propertyOperator.findGetter(enumType, value);
        Asserts.isNotNull(invoker, "cannot not find getter of property [{}] from [{}]", value, enumType);
        this.valueGetter = invoker::invoke;
        return this;
    }

    /**
     * Whether to enable resolves configuration from the specified {@link ContainerEnum} annotation.
     *
     * @param enableContainerEnumAnnotation enable container enum annotation
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> enableContainerEnumAnnotation(boolean enableContainerEnumAnnotation) {
        this.enableContainerEnumAnnotation = enableContainerEnumAnnotation;
        return this;
    }

    /**
     * Set processing strategy of when the key is duplicated.
     *
     * @param duplicateStrategy coverage strategy
     * @return this builder instance
     * @since 2.2.0
     */
    public EnumContainerBuilder<K, T> duplicateStrategy(DuplicateStrategy duplicateStrategy) {
        this.duplicateStrategy = duplicateStrategy;
        return this;
    }

    // ============== component ==============

    /**
     * Sets the annotation finder to use for reading the {@link ContainerEnum} annotation.
     *
     * @param annotationFinder the annotation finder to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> annotationFinder(@NonNull AnnotationFinder annotationFinder) {
        this.annotationFinder = annotationFinder;
        return this;
    }

    /**
     * Sets the property operator to use for reading properties from enum values.
     *
     * @param propertyOperator the property operator to set
     * @return this builder instance
     */
    public EnumContainerBuilder<K, T> propertyOperator(@NonNull PropertyOperator propertyOperator) {
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
        // read config from annotation
        if (enableContainerEnumAnnotation) {
            ContainerEnum annotation = annotationFinder.getAnnotation(enumType, ContainerEnum.class);
            if (Objects.nonNull(annotation)) {
                resolveConfigFromAnnotation(annotation);
            }
        }

        // build container
        Map<K, Object> enumMap = new HashMap<>(enumType.getEnumConstants().length);
        for (T e : enumType.getEnumConstants()) {
            K key = (K)keyGetter.apply(e);
            Object newVal = valueGetter.apply(e);
            enumMap.compute(key, (k, oldVal) ->
                Objects.isNull(oldVal) ? newVal : duplicateStrategy.choose(k, oldVal, newVal));
        }
        namespace = StringUtils.emptyToDefault(this.namespace, enumType.getSimpleName());
        return ImmutableMapContainer.forMap(namespace, enumMap);
    }

    private void resolveConfigFromAnnotation(ContainerEnum annotation) {
        if (namespace == null) {
            namespace = StringUtils.emptyToDefault(annotation.namespace(), enumType.getSimpleName());
        }
        duplicateStrategy = annotation.duplicateStrategy();
        boolean hasKey = StringUtils.isNotEmpty(annotation.key());
        boolean hasValue = StringUtils.isNotEmpty(annotation.value());
        if (hasKey && keyGetter == DEFAULT_KEY_GETTER) {
            keyGetter = e -> propertyOperator.readProperty(enumType, e, annotation.key());
        }
        if (hasValue && valueGetter == DEFAULT_VALUE_GETTER) {
            valueGetter = e -> propertyOperator.readProperty(enumType, e, annotation.value());
        }
    }
}