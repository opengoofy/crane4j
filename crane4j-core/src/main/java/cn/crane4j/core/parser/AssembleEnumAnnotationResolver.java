package cn.crane4j.core.parser;

import cn.crane4j.annotation.AssembleEnum;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.container.ConfigurableContainerProvider;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.StringUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link StandardAssembleAnnotationResolver} implementation for {@link AssembleEnum} annotation.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
public class AssembleEnumAnnotationResolver extends StandardAssembleAnnotationResolver<AssembleEnum> {

    private final ConfigurableContainerProvider containerProvider;
    private final PropertyOperator propertyOperator;

    /**
     * Create an {@link StandardAssembleAnnotationResolver} instance.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration globalConfiguration
     * @param propertyOperator property operator
     */
    public AssembleEnumAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator, ConfigurableContainerProvider containerProvider) {
        this(annotationFinder, Sorted.comparator(), globalConfiguration, propertyOperator, containerProvider);
    }

    /**
     * Create an {@link StandardAssembleAnnotationResolver} instance.
     *
     * @param annotationFinder    annotation finder
     * @param operationComparator operation comparator
     * @param globalConfiguration globalConfiguration
     * @param propertyOperator property operator
     */
    public AssembleEnumAnnotationResolver(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        Crane4jGlobalConfiguration globalConfiguration, PropertyOperator propertyOperator,
        ConfigurableContainerProvider containerProvider) {
        super(AssembleEnum.class, annotationFinder, operationComparator, globalConfiguration);
        this.propertyOperator = propertyOperator;
        this.containerProvider = containerProvider;
    }

    /**
     * Get namespace from annotation.
     *
     * @param annotation annotation
     * @return namespace
     */
    protected String getNamespace(AssembleEnum annotation) {
        return annotation.type() + "#" + annotation.enumKey() + "#" + annotation.enumValue();
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return {@link Container} instance
     */
    @Override
    protected Container<?> getContainer(AssembleEnum annotation) {
        Class<? extends Enum<?>> enumType = annotation.type();
        String namespace;
        if (annotation.useContainerEnum()) {
            ContainerEnum containerEnum = annotationFinder.findAnnotation(enumType, ContainerEnum.class);
            namespace = StringUtils.emptyToDefault(containerEnum.namespace(), containerEnum.getClass().getSimpleName());
        } else {
            namespace = getNamespace(annotation);
        }
        return containerProvider.getContainer(namespace, () -> createContainer(annotation, enumType, namespace));
    }

    private ConstantContainer<Object> createContainer(AssembleEnum annotation, Class<? extends Enum<?>> enumType, String namespace) {
        if (annotation.useContainerEnum()) {
            return ConstantContainer.forEnum(enumType, annotationFinder, propertyOperator);
        }
        boolean hasKey = StringUtils.isNotEmpty(annotation.enumKey());
        boolean hasValue = StringUtils.isNotEmpty(annotation.enumValue());
        return ConstantContainer.forMap(namespace, Stream.of(enumType.getEnumConstants())
            .collect(Collectors.toMap(e -> hasKey ? Objects.requireNonNull(propertyOperator.readProperty(enumType, e, annotation.enumKey())) : e, e -> hasValue ? Objects.requireNonNull(propertyOperator.readProperty(enumType, e, annotation.enumValue())) : e)));
    }

    /**
     * Get property mapping from given {@code attributes}.
     *
     * @param annotation annotation
     * @param attributes attributes
     * @param key key
     * @return assemble operation groups
     */
    @Override
    protected Set<PropertyMapping> parsePropertyMappings(AssembleEnum annotation, Map<String, Object> attributes, String key) {
        Set<PropertyMapping> propertyMappings = super.parsePropertyMappings(annotation, attributes, key);
        if (StringUtils.isNotEmpty(annotation.ref())) {
            propertyMappings.add(new SimplePropertyMapping("", annotation.ref()));
        }
        return propertyMappings;
    }
}
