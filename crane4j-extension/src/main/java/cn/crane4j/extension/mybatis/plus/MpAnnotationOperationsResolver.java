package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.AbstractCacheableOperationResolver;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperationsResolver;
import cn.crane4j.core.parser.DisassembleOperation;
import cn.crane4j.core.parser.KeyTriggerOperation;
import cn.crane4j.core.parser.OperationParseContext;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimpleAssembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.lang.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>The implementation of {@link BeanOperationsResolver}.<br />
 * It's used to scan the {@link AssembleMp} annotations on classes and their attributes,
 * And generate {@link AssembleOperation} for it
 * using {@link MpMethodContainer} as the data source container.
 *
 * @author huangchengxing
 * @see AssembleMp
 * @see MpBaseMapperContainerRegister
 */
public class MpAnnotationOperationsResolver extends AbstractCacheableOperationResolver {

    private final MpBaseMapperContainerRegister mapperContainerRegister;
    private final Crane4jGlobalConfiguration globalConfiguration;

    /**
     * Create a {@link MpAnnotationOperationsResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param mapperContainerRegister mp method container provider
     * @param globalConfiguration global configuration
     */
    public MpAnnotationOperationsResolver(
        AnnotationFinder annotationFinder,
        MpBaseMapperContainerRegister mapperContainerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, Sorted.comparator(), mapperContainerRegister, globalConfiguration);
    }

    /**
     * Create a {@link MpAnnotationOperationsResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param mapperContainerRegister mp method container provider
     * @param globalConfiguration global configuration
     */
    public MpAnnotationOperationsResolver(
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
     * @param beanType bean type
     * @return {@link AssembleOperation}
     * @see #parseAnnotationForDeclaredFields
     */
    @Override
    protected List<AssembleOperation> parseAssembleOperations(OperationParseContext context, Class<?> beanType) {
        List<AssembleMp> fieldLevelAnnotations = parseAnnotationForDeclaredFields(
            beanType, AssembleMp.class, (annotation, field) -> {
                ReflectUtils.setAttributeValue(annotation, "key", field.getName());
                return annotation;
            }
        );
        Set<AssembleMp> classLevelAnnotations = annotationFinder.findAllAnnotations(beanType, AssembleMp.class);
        fieldLevelAnnotations.addAll(classLevelAnnotations);
        return fieldLevelAnnotations.stream()
            .map(this::createAssembleOperation)
            .sorted(operationComparator)
            .collect(Collectors.toList());
    }

    private AssembleOperation createAssembleOperation(AssembleMp annotation) {
        // get operation handler
        AssembleOperationHandler handler = ConfigurationUtil.getAssembleOperationHandler(
            globalConfiguration, annotation.handlerName(), annotation.handler()
        );
        Assert.notNull(handler, throwException("assemble operation handler [{}]({}) not found", annotation.handlerName(), annotation.handler()));
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

    /**
     * Parse disassemble operations for class.
     *
     * @param context  context
     * @param beanType bean type
     * @return {@link DisassembleOperation}
     * @see #parseAnnotationForDeclaredFields
     */
    @Override
    protected List<DisassembleOperation> parseDisassembleOperations(OperationParseContext context, Class<?> beanType) {
        return Collections.emptyList();
    }
}
