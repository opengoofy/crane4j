package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.handler.AbstractAssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.OperationAnnotationHandler;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import lombok.experimental.Accessors;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Comparator;

/**
 * <p>The implementation of {@link OperationAnnotationHandler}.<br />
 * It's used to scan the {@link AssembleMp} annotations on classes and their attributes,
 * And generate {@link AssembleOperation} for it using {@link MybatisPlusQueryContainerProvider.Query} as the data source container.
 *
 * @author huangchengxing
 * @see AssembleMp
 * @see MybatisPlusQueryContainerProvider
 * @since 1.2.0
 */
@Accessors(chain = true)
public class AssembleMpAnnotationHandler extends AbstractAssembleAnnotationHandler<AssembleMp> {

    private static final String QUERY_CONTAINER_PROVIDER_NAME = "MybatisQueryContainerProvider";
    private final MybatisPlusQueryContainerProvider containerRegister;

    /**
     * Create a {@link AssembleMpAnnotationHandler} comparator.
     *
     * @param annotationFinder annotation finder
     * @param containerRegister mybatis plus query container register
     * @param globalConfiguration global configuration
     */
    public AssembleMpAnnotationHandler(
        AnnotationFinder annotationFinder,
        MybatisPlusQueryContainerProvider containerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, Crane4jGlobalSorter.comparator(), containerRegister, globalConfiguration);
    }

    /**
     * Create a {@link AssembleMpAnnotationHandler} comparator.
     *
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param containerRegister mybatis plus query container register
     * @param globalConfiguration global configuration
     */
    public AssembleMpAnnotationHandler(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        MybatisPlusQueryContainerProvider containerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        super(AssembleMp.class, annotationFinder, operationComparator, globalConfiguration);
        this.containerRegister = containerRegister;
        globalConfiguration.registerContainerProvider(QUERY_CONTAINER_PROVIDER_NAME, containerRegister);
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return namespace of {@link Container}
     */
    @Override
    protected String getContainerNamespace(AssembleMp annotation) {
        String namespace = containerRegister.determineNamespace(
            annotation.mapper(), annotation.where(), Arrays.asList(annotation.selects())
        );
        if (annotation.mappingType() != MappingType.ONE_TO_ONE) {
            containerRegister.setMappingType(namespace, annotation.mappingType());
        }
        return ContainerManager.canonicalNamespace(namespace, QUERY_CONTAINER_PROVIDER_NAME);
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
        BeanOperations beanOperations, AnnotatedElement element, AssembleMp annotation) {
        return new StandardAnnotationAdapter(
            annotation, annotation.key(), annotation.sort(),
            annotation.handler(),
            annotation.propTemplates(), annotation.props(), annotation.groups()
        );
    }
}
