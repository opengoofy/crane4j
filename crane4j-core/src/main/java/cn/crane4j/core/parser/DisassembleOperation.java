package cn.crane4j.core.parser;

import cn.crane4j.core.executor.handler.DisassembleOperationHandler;

import javax.annotation.Nonnull;

/**
 * 根据指定的key触发的拆卸操作，表示根据指定key值获取目标对象属性中的嵌套对象的一套流程配置信息。
 *
 * @author huangchengxing
 * @see TypeFixedDisassembleOperation
 * @see TypeDynamitedDisassembleOperation
 */
public interface DisassembleOperation extends KeyTriggerOperation {

    /**
     * 获取当前待处理的嵌套对象所在的源对象的类型
     *
     * @return 操作所属的源对象的类型
     */
    Class<?> getSourceType();

    /**
     * 获取嵌套对象的操作配置
     *
     * @param internalBean 带解析的嵌套对象
     * @return 嵌套对象的操作配置
     */
    @Nonnull
    BeanOperations getInternalBeanOperations(Object internalBean);

    /**
     * 获取当前拆卸操作的处理器
     *
     * @return 拆卸处理器
     */
    DisassembleOperationHandler getDisassembleOperationHandler();
}
