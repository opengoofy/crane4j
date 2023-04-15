package cn.crane4j.core.parser;

import cn.crane4j.core.support.Sorted;

import java.lang.reflect.AnnotatedElement;

/**
 * Class operation resolver, belonging to {@link TypeHierarchyBeanOperationParser},
 * used to obtain all operation configurations in a single parsing of a class.
 *
 * @author huangchengxing
 * @see AssembleAnnotationOperationsResolver
 * @see DisassembleAnnotationOperationsResolver
 * @since 1.2.0
 */
public interface BeanOperationsResolver extends Sorted {

    /**
     * Resolve operations from type
     *
     * @param context context
     * @param annotatedElement annotated element
     */
    void resolve(OperationParseContext context, AnnotatedElement annotatedElement);
}
