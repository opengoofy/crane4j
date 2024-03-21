package cn.crane4j.core.parser.handler;

import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.Sorted;

/**
 * <p>用于处理元素上的注解，将其解析为{@link AssembleOperation}或{@link DisassembleOperation}的处理器。
 *
 * <p>通常情况下，一个处理器通常只解析元素上的某种特定注解，
 * 例如{@link AssembleAnnotationHandler}只用于从元素中解析{@link AssembleOperation}。
 * 不同的处理器将会被注册到{@link TypeHierarchyBeanOperationParser 配置解析器}中，
 * 最终由配置解析器来依次调用注解处理器来彻底的解析元素上的所有的注解配置。
 *
 * <p>对应实现类，推荐继承{@link AbstractStandardOperationAnnotationHandler}类，
 * 或{@link AbstractStandardAssembleAnnotationHandler}类，
 * 这两个模板类都预先实现了标准组件的解析和构建{@link KeyTriggerOperation}的逻辑，
 * 并且提供了一些便捷的方法来从标准注解中获取通用的配置信息。
 *
 * <hr/>
 *
 * <p>A handler that resolves {@link AssembleOperation} or {@link DisassembleOperation}
 * from annotation on the element to {@link BeanOperations#getSource()}.
 *
 * <p>Generally, a handler typically only resolves the specific annotation from the element,
 * for example, {@link AssembleAnnotationHandler} resolve {@link AssembleOperation} from the element.
 * {@link TypeHierarchyBeanOperationParser} will hold multiple resolvers to resolve different annotations.
 *
 * <p>For implementors of assemble operation annotation handler,
 * it is recommended to derive from the provided {@link AbstractStandardAssembleAnnotationHandler} class,
 * which pre-implements the defined the logic of parsing and
 * constructing {@link AssembleOperation} based on standard components.
 *
 * @author huangchengxing
 * @see AbstractStandardAssembleAnnotationHandler
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
