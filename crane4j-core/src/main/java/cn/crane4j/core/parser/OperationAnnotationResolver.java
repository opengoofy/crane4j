package cn.crane4j.core.parser;

import cn.crane4j.core.support.Sorted;

/**
 * <p>A resolver that resolves {@link AssembleOperation} or {@link DisassembleOperation}
 * from annotation on the element to {@link BeanOperations#getSource()}.
 *
 * <p>Generally, a resolver typically only resolve one or two specific annotations from the element,
 * for example, {@link AssembleAnnotationResolver} resolve {@link AssembleOperation} from the element.
 * {@link TypeHierarchyBeanOperationParser} will hold multiple resolvers to resolve different annotations.
 *
 * @author huangchengxing
 * @see TypeHierarchyBeanOperationParser
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
