package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleEnum;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.EnumContainerBuilder;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.Set;

/**
 * An {@link AbstractAssembleAnnotationHandler} implementation for {@link AssembleEnum} annotation.
 *
 * @author huangchengxing
 * @see AssembleEnum
 * @since 1.3.0
 */
@Slf4j
public class AssembleEnumAnnotationHandler
    extends InternalProviderAssembleAnnotationHandler<AssembleEnum> {

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
        PropertyOperator propertyOperator,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        this(annotationFinder, Crane4jGlobalSorter.comparator(), globalConfiguration, propertyOperator, propertyMappingStrategyManager);
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
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(AssembleEnum.class, annotationFinder, operationComparator, globalConfiguration, propertyMappingStrategyManager);
        this.propertyOperator = propertyOperator;
    }

    /**
     * Create container by given annotation and namespace.
     *
     * @param annotation annotation
     * @param namespace  namespace
     * @return {@link Container} instant
     */
    @Override
    protected @NonNull Container<Object> createContainer(AssembleEnum annotation, String namespace) {
        Class<? extends Enum<?>> enumType = resolveEnumType(annotation);
        EnumContainerBuilder<Object, ? extends Enum<?>> builder = EnumContainerBuilder.of(enumType)
            .namespace(namespace)
            .annotationFinder(annotationFinder)
            .propertyOperator(propertyOperator);
        // follow the configuration of the annotation which specified in the @AssembleEnum annotation
        if (annotation.followTypeConfig()) {
            return builder.build();
        }
        ContainerEnum containerEnum = annotation.enums();
        builder.key(containerEnum.key())
            .value(containerEnum.value())
            .duplicateStrategy(containerEnum.duplicateStrategy());
        return builder.build();
    }

    /**
     * Get namespace from annotation.
     *
     * @param annotation annotation
     * @return namespace
     */
    @Override
    protected String determineNamespace(AssembleEnum annotation) {
        Class<? extends Enum<?>> enumType = resolveEnumType(annotation);
        String config = annotation.followTypeConfig() ? "FollowTypeConfig" : annotation.enums().toString();
        return StringUtils.md5DigestAsHex(StringUtils.join(
            String::valueOf, "#", enumType, config
        ));
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Enum<?>> resolveEnumType(AssembleEnum annotation) {
        Class<?> type = annotation.type();
        if (ClassUtils.isObjectOrVoid(type)) {
            type = ClassUtils.forName(annotation.typeName(), type);
            Asserts.isTrue(type.isEnum(), "type [{}] which specified in @AssembleEnum is not a enum type", type.getName());
        }
        return (Class<? extends Enum<?>>)type;
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
            annotation, annotation.key(), annotation.keyType(), annotation.sort(),
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
