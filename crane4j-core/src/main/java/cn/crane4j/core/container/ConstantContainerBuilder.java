package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ObjectUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A builder class for creating {@link Container}s from public static constants in a specified class.
 *
 * @author tangcent
 * @see ContainerConstant
 */
public class ConstantContainerBuilder {

    private final Class<?> constantClass;
    private AnnotationFinder annotationFinder;
    private Boolean onlyPublic = null;
    private Boolean onlyExplicitlyIncluded = null;
    private Boolean reverse = null;
    private String namespace = null;

    /**
     * Creates a new instance of the builder with the specified constant class.
     *
     * @param constantClass the class containing the public static constants
     */
    private ConstantContainerBuilder(Class<?> constantClass) {
        this.constantClass = constantClass;
    }

    /**
     * Returns a new builder for the specified constant class.
     *
     * @param constantClass the class containing the public static constants
     * @return a new builder instance
     */
    public static ConstantContainerBuilder of(Class<?> constantClass) {
        return new ConstantContainerBuilder(constantClass);
    }

    /**
     * Sets the annotation finder to use when reading annotations.
     *
     * @param annotationFinder the annotation finder to use
     * @return this builder instance
     */
    public ConstantContainerBuilder annotationFinder(AnnotationFinder annotationFinder) {
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
        requireNonNull(constantClass);
        requireNonNull(annotationFinder);

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
        boolean onlyPublic = ObjectUtils.defaultIfNull(this.onlyPublic, true);
        boolean onlyExplicitlyIncluded = ObjectUtils.defaultIfNull(this.onlyExplicitlyIncluded, false);
        boolean reverse = ObjectUtils.defaultIfNull(this.reverse, false);

        // build fieldFilter
        Predicate<Field> fieldFilter = field -> Modifier.isStatic(field.getModifiers());
        if (onlyPublic) {
            fieldFilter = fieldFilter.and(field -> Modifier.isPublic(field.getModifiers()));
        }
        if (onlyExplicitlyIncluded) {
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
        String namespace = StringUtils.emptyToDefault(this.namespace, constantClass.getSimpleName());
        return ImmutableMapContainer.forMap(namespace, reverse ? CollectionUtils.reverse(data) : data);
    }
}