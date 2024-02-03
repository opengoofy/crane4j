package cn.crane4j.core.parser.handler;

import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.support.Sorted;

/**
 * <p>A handler that resolves {@link AssembleOperation} or {@link DisassembleOperation}
 * from annotation on the element to {@link BeanOperations#getSource()}.
 *
 * <p>Generally, a handler typically only resolves the specific annotation from the element,
 * for example, {@link AssembleAnnotationHandler} resolve {@link AssembleOperation} from the element.
 * {@link TypeHierarchyBeanOperationParser} will hold multiple resolvers to resolve different annotations.
 *
 * <p>For implementors of assemble operation annotation handler,
 * it is recommended to derive from the provided {@link AbstractAssembleAnnotationHandler} class,
 * which pre-implements the defined the logic of parsing and
 * constructing {@link AssembleOperation} based on standard components.
 *
 * @author huangchengxing
 * @see AbstractAssembleAnnotationHandler
 * @see TypeHierarchyBeanOperationParser
 * @since 1.2.0
 */
public interface OperationAnnotationHandler extends Sorted {

    /**
     * Resolve operations from element.
     *
     * @param parser parser
     * @param beanOperations bean operations to be handler
     */
    void resolve(BeanOperationParser parser, BeanOperations beanOperations);
}
