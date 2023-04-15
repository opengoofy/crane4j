package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.*;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.lang.Assert;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>The implementation of {@link OperationAnnotationResolver}.<br />
 * It's used to scan the {@link AssembleMp} annotations on classes and their attributes,
 * And generate {@link AssembleOperation} for it
 * using {@link MpMethodContainer} as the data source container.
 *
 * @author huangchengxing
 * @see AssembleMp
 * @see MpBaseMapperContainerRegister
 * @since 1.2.0
 */
public class AssembleMpAnnotationResolver extends AbstractCacheableOperationAnnotationResolver {

    private final MpBaseMapperContainerRegister mapperContainerRegister;
    private final Crane4jGlobalConfiguration globalConfiguration;

    /**
     * Create a {@link AssembleMpAnnotationResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param mapperContainerRegister mp method container provider
     * @param globalConfiguration global configuration
     */
    public AssembleMpAnnotationResolver(
        AnnotationFinder annotationFinder,
        MpBaseMapperContainerRegister mapperContainerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, Sorted.comparator(), mapperContainerRegister, globalConfiguration);
    }

    /**
     * Create a {@link AssembleMpAnnotationResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param mapperContainerRegister mp method container provider
     * @param globalConfiguration global configuration
     */
    public AssembleMpAnnotationResolver(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        MpBaseMapperContainerRegister mapperContainerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        super(annotationFinder, operationComparator);
        this.mapperContainerRegister = mapperContainerRegister;
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * Parse assemble operations for class.
     *
     * @param context  context
     * @param element annotated element
     * @return {@link AssembleOperation}
     */
    @Override
    protected List<AssembleOperation> parseAssembleOperations(OperationParseContext context, AnnotatedElement element) {
        List<AssembleMp> annotations = new ArrayList<>();
        if (element instanceof Class<?>) {
            Class<?> beanType = (Class<?>)element;
            annotations.addAll(parseAnnotationForDeclaredFields(beanType));
            annotations.addAll(annotationFinder.getAllAnnotations(beanType, AssembleMp.class));
        } else {
            annotations.addAll(annotationFinder.findAllAnnotations(element, AssembleMp.class));
        }
        return annotations.stream()
            .map(this::createAssembleOperation)
            .sorted(operationComparator)
            .collect(Collectors.toList());
    }

    private List<AssembleMp> parseAnnotationForDeclaredFields(Class<?> beanType) {
        return ReflectUtils.parseAnnotationForDeclaredFields(annotationFinder, beanType, AssembleMp.class, (annotation, field) -> {
            ReflectUtils.setAttributeValue(annotation, "key", field.getName());
            return annotation;
        });
    }

    private AssembleOperation createAssembleOperation(AssembleMp annotation) {
        // get operation handler
        AssembleOperationHandler handler = ConfigurationUtil.getAssembleOperationHandler(
            globalConfiguration, annotation.handlerName(), annotation.handler()
        );
        Assert.notNull(handler, AbstractCacheableOperationAnnotationResolver.throwException("assemble operation handler [{}]({}) not found", annotation.handlerName(), annotation.handler()));
        // get container
        Container<?> container = mapperContainerRegister.getContainer(
            annotation.mapper(), annotation.where(), Arrays.asList(annotation.selects())
        );
        // parse property mapping
        Set<PropertyMapping> propertyMappings = Stream.of(annotation.props())
            .map(ConfigurationUtil::createPropertyMapping)
            .collect(Collectors.toSet());
        propertyMappings.addAll(ConfigurationUtil.parsePropTemplateClasses(annotation.propTemplates(), annotationFinder));
        SimpleAssembleOperation operation = new SimpleAssembleOperation(
            annotation.key(), annotation.sort(), propertyMappings, container, handler
        );
        // add groups
        operation.getGroups().addAll(Arrays.asList(annotation.groups()));
        return operation;
    }
}