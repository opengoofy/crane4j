package cn.crane4j.core.parser;

import cn.crane4j.core.support.Sorted;

/**
 * Class operation resolver, belonging to {@link TypeHierarchyBeanOperationParser},
 * used to obtain all operation configurations in a single parsing of a class.
 *
 * @author huangchengxing
 * @see AssembleAnnotationResolver
 * @see DisassembleAnnotationResolver
 * @since 1.2.0
 */
public interface OperationAnnotationResolver extends Sorted {

    /**
     * Resolve operations from type
     *
     * @param parser parser
     * @param beanOperations bean operations to be resolver
     */
    void resolve(BeanOperationParser parser, BeanOperations beanOperations);
}
