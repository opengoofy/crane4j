package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ObjectUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A builder class for creating {@link Container}s from public static constants in a specified class.
 *
 * @author tangcent
 * @see ContainerConstant
 * @since 2.0.0
 */
public class ConstantContainerBuilder {

    /**
     * The class containing the public static constants.
     */
    @NonNull
    private final Class<?> constantClass;

    /**
     * Whether to include only public constants.
     */
    @Nullable
    private Boolean onlyPublic = null;

    /**
     * Whether to include only constants that are explicitly annotated with {@link ContainerConstant.Include}.
     */
    @Nullable
    private Boolean onlyExplicitlyIncluded = null;

    /**
     * Whether to reverse the order of the constant values in the container.
     */
    @Nullable
    private Boolean reverse = null;

    /**
     * The namespace for the container.
     */
    @Nullable
    private String namespace = null;

    /**
     * The annotation finder to use when reading {@link ContainerConstant} annotation.
     */
    @NonNull
    private AnnotationFinder annotationFinder = SimpleAnnotationFinder.INSTANCE;

    /**
     * Creates a new instance of the builder with the specified constant class.
     *
     * @param constantClass the class containing the public static constants
     */
    private ConstantContainerBuilder(@NonNull Class<?> constantClass) {
        this.constantClass = Objects.requireNonNull(constantClass, "constantClass must not null");
    }

    /**
     * Returns a new builder for the specified constant class.
     *
     * @param constantClass the class containing the public static constants
     * @return a new builder instance
     */
    public static ConstantContainerBuilder of(@NonNull Class<?> constantClass) {
        return new ConstantContainerBuilder(constantClass);
    }

    /**
     * Sets the annotation finder to use when reading annotations.
     *
     * @param annotationFinder the annotation finder to use
     * @return this builder instance
     */
    public ConstantContainerBuilder annotationFinder(@NonNull AnnotationFinder annotationFinder) {
        this.annotationFinder = annotationFinder;
        return this;
    }

    /**
     * Sets whether to include only public constants.
     *
     * @param onlyPublic whether to include only public constants
     * @return this builder instance
     */
    public ConstantContainerBuilder onlyPublic(boolean onlyPublic) {
        this.onlyPublic = onlyPublic;
        return this;
    }

    /**
     * Sets whether to include only constants that are explicitly annotated with {@link ContainerConstant.Include}.
     *
     * @param onlyExplicitlyIncluded whether to include only explicitly included constants
     * @return this builder instance
     */
    public ConstantContainerBuilder onlyExplicitlyIncluded(boolean onlyExplicitlyIncluded) {
        this.onlyExplicitlyIncluded = onlyExplicitlyIncluded;
        return this;
    }

    /**
     * Sets whether to reverse the order of the constant values in the container.
     *
     * @param reverse whether to reverse the order of the constant values
     * @return this builder instance
     */
    public ConstantContainerBuilder reverse(boolean reverse) {
        this.reverse = reverse;
        return this;
    }

    /**
     * Sets the namespace of the container.
     *
     * @param namespace the namespace
     * @return this builder instance
     */
    public ConstantContainerBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Builds and returns an immutable container from the specified constant class and builder configuration.
     *
     * @return an immutable container
     */
    public Container<Object> build() {
        // process annotation
        ContainerConstant annotation = annotationFinder.getAnnotation(constantClass, ContainerConstant.class);
        if (annotation != null) {
            if (this.onlyPublic == null) {
                this.onlyPublic = annotation.onlyPublic();
            }
            if (this.onlyExplicitlyIncluded == null) {
                this.onlyExplicitlyIncluded = annotation.onlyExplicitlyIncluded();
            }
            if (this.reverse == null) {
                this.reverse = annotation.reverse();
            }
            if (this.namespace == null) {
                this.namespace = annotation.namespace();
            }
        }

        // use default values
        boolean isOnlyPublic = ObjectUtils.defaultIfNull(this.onlyPublic, true);
        boolean isOnlyExplicitlyIncluded = ObjectUtils.defaultIfNull(this.onlyExplicitlyIncluded, false);
        boolean isReverse = ObjectUtils.defaultIfNull(this.reverse, false);

        // build fieldFilter
        Predicate<Field> fieldFilter = field -> Modifier.isStatic(field.getModifiers());
        if (isOnlyPublic) {
            fieldFilter = fieldFilter.and(field -> Modifier.isPublic(field.getModifiers()));
        }
        if (isOnlyExplicitlyIncluded) {
            fieldFilter = fieldFilter.and(field -> annotationFinder.hasAnnotation(field, ContainerConstant.Include.class));
        }
        fieldFilter = fieldFilter.and(field -> !annotationFinder.hasAnnotation(field, ContainerConstant.Exclude.class));

        // get attribute
        Field[] fields = ReflectUtils.getFields(constantClass);
        Map<Object, Object> data = new LinkedHashMap<>();
        Stream.of(fields)
            .filter(fieldFilter)
            .forEach(field -> {
                Object value = ReflectUtils.getFieldValue(null, field);
                ContainerConstant.Name name = annotationFinder.getAnnotation(field, ContainerConstant.Name.class);
                String key = Objects.isNull(name) ? field.getName() : name.value();
                data.put(key, value);
            });

        // build container
        String actualNamespace = StringUtils.emptyToDefault(this.namespace, constantClass.getSimpleName());
        return ImmutableMapContainer.forMap(actualNamespace, isReverse ? CollectionUtils.reverse(data) : data);
    }
}