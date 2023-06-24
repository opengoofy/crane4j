package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.StringUtils;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;

/**
 * <p>Annotation-based {@link OperationAnnotationHandler} implementation
 * that the construction of operation configuration
 * by resolving annotations based on {@link Assemble} on classes and attributes.
 *
 * @author huangchengxing
 * @see Assemble
 * @since 1.2.0
 */
@Accessors(chain = true)
@Slf4j
public class AssembleAnnotationHandler extends AbstractAssembleAnnotationHandler<Assemble> {

    /**
     * Create a {@link AssembleAnnotationHandler} instance.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param operationComparator operation comparator
     */
    public AssembleAnnotationHandler(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        Comparator<KeyTriggerOperation> operationComparator) {
        super(Assemble.class, annotationFinder, operationComparator, globalConfiguration);
    }

    /**
     * <p>Create a {@link AssembleAnnotationHandler} instance.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public AssembleAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Crane4jGlobalSorter.instance());
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return namespace of {@link Container}
     */
    @Override
    protected String getContainerNamespace(Assemble annotation) {
        String namespace = annotation.container();
        if (StringUtils.isEmpty(namespace)) {
            return Container.EMPTY_CONTAINER_NAMESPACE;
        }
        return ContainerManager.canonicalNamespace(namespace, annotation.containerProvider());
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
        BeanOperations beanOperations, AnnotatedElement element, Assemble annotation) {
        return new StandardAnnotationAdapter(
            annotation, annotation.key(), annotation.sort(),
            annotation.handler(),
            annotation.propTemplates(), annotation.props(), annotation.groups()
        );
    }
}
