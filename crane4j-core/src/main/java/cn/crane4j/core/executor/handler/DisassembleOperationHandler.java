package cn.crane4j.core.executor.handler;

import cn.crane4j.core.parser.DisassembleOperation;

import java.util.Collection;

/**
 * 拆卸操作处理器
 *
 * @author huangchengxing
 * @see ReflectAssembleOperationHandler
 */
public interface DisassembleOperationHandler {

    /**
     * 根据拆卸配置，提取出对象属性中的嵌套对象
     *
     * @param operation 要执行的拆卸操作
     * @param targets 待处理的目标对象，类型应当与{@code targetType}一致
     */
    Collection<?> process(DisassembleOperation operation, Collection<?> targets);
}
