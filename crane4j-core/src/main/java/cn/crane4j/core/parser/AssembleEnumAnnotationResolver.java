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

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link AbstractAssembleAnnotationResolver} implementation for {@link AssembleEnum} annotation.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
public class AssembleEnumAnnotationResolver extends AbstractAssembleAnnotationResolver<AssembleEnum> {

    private final ConfigurableContainerProvider containerProvider;
    private final PropertyOperator propertyOperator;

    /**
     * Create an {@link AbstractAssembleAnnotationResolver} instance.
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
     * Create an {@link AbstractAssembleAnnotationResolver} instance.
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

    /**
     * Get {@link StandardAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element        element
     * @param annotation     annotation
     * @return {@link StandardAnnotation} instance
     */
    @Override
    protected StandardAnnotation getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleEnum annotation) {
        return new StandardAnnotationAdapter(
            annotation, annotation.key(), annotation.sort(),
            annotation.handlerName(), annotation.handler(),
            annotation.propTemplates(), annotation.props(), annotation.groups()
        );
    }

    /**
     * Get property mapping from given {@link StandardAnnotation}.
     *
     * @param standardAnnotation standard annotation
     * @param key key
     * @return assemble operation groups
     */
    @Override
    protected Set<PropertyMapping> parsePropertyMappings(StandardAnnotation standardAnnotation, String key) {
        Set<PropertyMapping> propertyMappings = super.parsePropertyMappings(standardAnnotation, key);
        AssembleEnum annotation = (AssembleEnum)((StandardAnnotationAdapter)standardAnnotation).getAnnotation();
        if (StringUtils.isNotEmpty(annotation.ref())) {
            propertyMappings.add(new SimplePropertyMapping("", annotation.ref()));
        }
        return propertyMappings;
    }

    private ConstantContainer<Object> createContainer(
        AssembleEnum annotation, Class<? extends Enum<?>> enumType, String namespace) {
        if (annotation.useContainerEnum()) {
            return ConstantContainer.forEnum(enumType, annotationFinder, propertyOperator);
        }
        boolean hasKey = StringUtils.isNotEmpty(annotation.enumKey());
        boolean hasValue = StringUtils.isNotEmpty(annotation.enumValue());
        return ConstantContainer.forMap(namespace, Stream.of(enumType.getEnumConstants())
            .collect(Collectors.toMap(e -> hasKey ? Objects.requireNonNull(propertyOperator.readProperty(enumType, e, annotation.enumKey())) : e, e -> hasValue ? Objects.requireNonNull(propertyOperator.readProperty(enumType, e, annotation.enumValue())) : e)));
    }
}
