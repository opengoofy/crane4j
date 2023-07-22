package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleEnum;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.EnumContainerBuilder;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@link AbstractAssembleAnnotationHandler} implementation for {@link AssembleEnum} annotation.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
public class AssembleEnumAnnotationHandler extends AbstractAssembleAnnotationHandler<AssembleEnum> {

    private static final String INTERNAL_ENUM_CONTAINER_PROVIDER = "InternalEnumContainerProvider";
    private final InternalEnumContainerProvider internalEnumContainerProvider = new InternalEnumContainerProvider();
    private final PropertyOperator propertyOperator;

    /**
     * Create an {@link AbstractAssembleAnnotationHandler} comparator.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration globalConfiguration
     * @param propertyOperator    property operator
     */
    public AssembleEnumAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator, ContainerManager containerManager) {
        this(annotationFinder, Crane4jGlobalSorter.comparator(), globalConfiguration, propertyOperator, containerManager);
    }

    /**
     * Create an {@link AbstractAssembleAnnotationHandler} comparator.
     *
     * @param annotationFinder    annotation finder
     * @param operationComparator operation comparator
     * @param globalConfiguration globalConfiguration
     * @param propertyOperator    property operator
     */
    public AssembleEnumAnnotationHandler(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        Crane4jGlobalConfiguration globalConfiguration, PropertyOperator propertyOperator,
        ContainerManager containerManager) {
        super(AssembleEnum.class, annotationFinder, operationComparator, globalConfiguration);
        this.propertyOperator = propertyOperator;
        containerManager.registerContainerProvider(INTERNAL_ENUM_CONTAINER_PROVIDER, internalEnumContainerProvider);
    }

    /**
     * Get namespace from annotation.
     *
     * @param annotation annotation
     * @return namespace
     */
    protected String getNamespace(AssembleEnum annotation) {
        return StringUtils.format(
            "@{}:{}#{}#{}", getClass().getSimpleName(), annotation.type(), annotation.enumKey(), annotation.enumValue()
        );
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return namespace of {@link Container}
     */
    @Override
    protected String getContainerNamespace(AssembleEnum annotation) {
        Class<? extends Enum<?>> enumType = annotation.type();
        EnumContainerBuilder<Object, ? extends Enum<?>> enumContainerBuilder = EnumContainerBuilder.of(enumType)
            .annotationFinder(annotationFinder)
            .propertyOperator(propertyOperator);
        if (!annotation.useContainerEnum()) {
            enumContainerBuilder.key(annotation.enumKey());
            enumContainerBuilder.value(annotation.enumValue());
            enumContainerBuilder.namespace(getNamespace(annotation));
        }
        Container<Object> container = enumContainerBuilder
            .build();

        // register to container manager
        String namespace = container.getNamespace();
        CollectionUtils.computeIfAbsent(
            internalEnumContainerProvider.enumCaches,
            namespace, ns -> container
        );
        return ContainerManager.canonicalNamespace(namespace, INTERNAL_ENUM_CONTAINER_PROVIDER);
    }

    /**
     * Get {@link StandardAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element        element
     * @param annotation     annotation
     * @return {@link StandardAnnotation} comparator
     */
    @Override
    protected StandardAnnotation getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleEnum annotation) {
        return new StandardAnnotationAdapter(
            annotation, annotation.key(), annotation.sort(),
            annotation.handler(), annotation.handlerType(),
            annotation.propTemplates(), annotation.props(), annotation.groups()
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

    private static class InternalEnumContainerProvider implements ContainerProvider {

        private final Map<String, Container<Object>> enumCaches = new ConcurrentHashMap<>();

        /**
         * Get container comparator by given namespace
         *
         * @param namespace namespace of container
         * @return container comparator
         */
        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public <K> Container<K> getContainer(String namespace) {
            return (Container<K>) enumCaches.get(namespace);
        }

        /**
         * Whether this provider has container of given {@code namespace}.
         *
         * @param namespace namespace
         * @return boolean
         */
        @Override
        public boolean containsContainer(String namespace) {
            return enumCaches.containsKey(namespace);
        }
    }
}
