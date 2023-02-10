package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.executor.handler.DisassembleOperationHandler;
import lombok.Getter;
import lombok.NonNull;

/**
 * {@link DisassembleOperation}的简单实现，用于固定类型的嵌套对象拆卸配置
 *
 * @author huangchengxing
 */
public class TypeFixedDisassembleOperation extends SimpleKeyTriggerOperation implements DisassembleOperation {

    @Getter
    private final Class<?> sourceType;
    @Getter
    private final DisassembleOperationHandler disassembleOperationHandler;
    private final BeanOperations internalBeanOperations;

    public TypeFixedDisassembleOperation(
        String key, int sort, Class<?> sourceType,
        BeanOperations internalBeanOperations,
        DisassembleOperationHandler disassembleOperationHandler) {
        super(key, sort);
        this.sourceType = sourceType;
        this.internalBeanOperations = internalBeanOperations;
        this.disassembleOperationHandler = disassembleOperationHandler;
    }

    public TypeFixedDisassembleOperation(
        String key, Class<?> sourceType,
        BeanOperations internalBeanOperations,
        DisassembleOperationHandler disassembleOperationHandler) {
        this(key, Integer.MAX_VALUE, sourceType, internalBeanOperations, disassembleOperationHandler);
    }

    /**
     * 获取嵌套对象的操作配置
     *
     * @param internalBean 带解析的嵌套对象
     * @return 嵌套对象的操作配置
     */
    @NonNull
    @Override
    public BeanOperations getInternalBeanOperations(Object internalBean) {
        return internalBeanOperations;
    }
}
