package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleEnum;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.EnumContainerBuilder;
import cn.crane4j.core.container.PartitionContainerProvider;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.Set;

/**
 * An {@link AbstractAssembleAnnotationHandler} implementation for {@link AssembleEnum} annotation.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
@Slf4j
public class AssembleEnumAnnotationHandler extends AbstractAssembleAnnotationHandler<AssembleEnum> {

    private static final String INTERNAL_ENUM_CONTAINER_PROVIDER = "InternalEnumContainerProvider";
    private final PartitionContainerProvider internalContainerProvider = new PartitionContainerProvider();
    private final PropertyOperator propertyOperator;

    /**
     * Create an {@link AbstractAssembleAnnotationHandler} instance.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration globalConfiguration
     * @param propertyOperator    property operator
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleEnumAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator, ContainerManager containerManager,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        this(annotationFinder, Crane4jGlobalSorter.comparator(), globalConfiguration, propertyOperator, containerManager, propertyMappingStrategyManager);
    }

    /**
     * Create an {@link AbstractAssembleAnnotationHandler} instance.
     *
     * @param annotationFinder    annotation finder
     * @param operationComparator operation comparator
     * @param globalConfiguration globalConfiguration
     * @param propertyOperator    property operator
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleEnumAnnotationHandler(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        Crane4jGlobalConfiguration globalConfiguration, PropertyOperator propertyOperator,
        ContainerManager containerManager,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(AssembleEnum.class, annotationFinder, operationComparator, globalConfiguration, propertyMappingStrategyManager);
        this.propertyOperator = propertyOperator;
        containerManager.registerContainerProvider(INTERNAL_ENUM_CONTAINER_PROVIDER, internalContainerProvider);
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return namespace of {@link Container}
     */
    @Override
    protected String getContainerNamespace(AssembleEnum annotation) {
        // if container exists, return namespace
        String namespace = determineNamespace(annotation);
        if (internalContainerProvider.containsContainer(namespace)) {
            return namespace;
        }
        Container<Object> container = doGetContainer(annotation, namespace);
        internalContainerProvider.registerContainer(container);
        return ContainerManager.canonicalNamespace(container.getNamespace(), INTERNAL_ENUM_CONTAINER_PROVIDER);
    }

    private Container<Object> doGetContainer(AssembleEnum annotation, String namespace) {
        Class<? extends Enum<?>> enumType = annotation.type();
        EnumContainerBuilder<Object, ? extends Enum<?>> enumContainerBuilder = EnumContainerBuilder.of(enumType)
            .namespace(namespace)
            .annotationFinder(annotationFinder)
            .propertyOperator(propertyOperator);
        // not using @ContainerEnum config?
        if (!annotation.useContainerEnum()) {
            if (StringUtils.isNotEmpty(annotation.enumKey())) {
                enumContainerBuilder.key(annotation.enumKey());
            }
            if (StringUtils.isNotEmpty(annotation.enumValue())) {
                enumContainerBuilder.value(annotation.enumValue());
            }
        }
        return enumContainerBuilder.build();
    }

    /**
     * Get namespace from annotation.
     *
     * @param annotation annotation
     * @return namespace
     */
    protected String determineNamespace(AssembleEnum annotation) {
        return StringUtils.md5DigestAsHex(StringUtils.join(
            String::valueOf, "#", annotation.enumKey(), annotation.enumValue(), annotation.type()
        ));
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
            annotation.handler(), annotation.handlerType(),
            annotation.propTemplates(), annotation.props(), annotation.groups(),
            annotation.propertyMappingStrategy()
        );
    }

    /**
     * Get property mapping from given {@link StandardAnnotation}.
     *
     * @param element            element
     * @param standardAnnotation standard annotation
     * @param key                key
     * @return assemble operation groups
     */
    @Override
    protected Set<PropertyMapping> parsePropertyMappings(AnnotatedElement element, StandardAnnotation standardAnnotation, String key) {
        Set<PropertyMapping> propertyMappings = super.parsePropertyMappings(element, standardAnnotation, key);
        AssembleEnum annotation = (AssembleEnum) ((StandardAnnotationAdapter) standardAnnotation).getAnnotation();
        if (StringUtils.isNotEmpty(annotation.ref())) {
            propertyMappings.add(new SimplePropertyMapping("", annotation.ref()));
        }
        return propertyMappings;
    }
}
