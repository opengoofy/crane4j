package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.handler.AbstractAssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.OperationAnnotationHandler;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import lombok.experimental.Accessors;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Comparator;

/**
 * <p>The implementation of {@link OperationAnnotationHandler}.<br />
 * It's used to scan the {@link AssembleMp} annotations on classes and their attributes,
 * And generate {@link AssembleOperation} for it using {@link MybatisPlusQueryContainerRegister.Query} as the data source container.
 *
 * @author huangchengxing
 * @see AssembleMp
 * @see MybatisPlusQueryContainerRegister
 * @since 1.2.0
 */
@Accessors(chain = true)
public class AssembleMpAnnotationHandler extends AbstractAssembleAnnotationHandler<AssembleMp> {

    private final MybatisPlusQueryContainerRegister containerRegister;

    /**
     * Create a {@link AssembleMpAnnotationHandler} instance.
     *
     * @param annotationFinder annotation finder
     * @param containerRegister mybatis plus query container register
     * @param globalConfiguration global configuration
     */
    public AssembleMpAnnotationHandler(
        AnnotationFinder annotationFinder,
        MybatisPlusQueryContainerRegister containerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, Sorted.comparator(), containerRegister, globalConfiguration);
    }

    /**
     * Create a {@link AssembleMpAnnotationHandler} instance.
     *
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param containerRegister mybatis plus query container register
     * @param globalConfiguration global configuration
     */
    public AssembleMpAnnotationHandler(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        MybatisPlusQueryContainerRegister containerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        super(AssembleMp.class, annotationFinder, operationComparator, globalConfiguration);
        this.containerRegister = containerRegister;
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return namespace of {@link Container}
     */
    @Override
    protected String getContainerNamespace(AssembleMp annotation) {
        return containerRegister.getContainer(
            annotation.mapper(), annotation.where(), Arrays.asList(annotation.selects())
        );
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
        BeanOperations beanOperations, AnnotatedElement element, AssembleMp annotation) {
        return new StandardAnnotationAdapter(
            annotation, annotation.key(), annotation.sort(),
            annotation.handler(),
            annotation.propTemplates(), annotation.props(), annotation.groups()
        );
    }
}
