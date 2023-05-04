package cn.crane4j.core.parser;

import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>The {@link DisassembleOperation} implementation that express the disassemble operation of data from fixed type.
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
     * <p>Get the operation configuration of nested object.<br />
     * Always return a fixed type.
     *
     * @param internalBean internal bean
     * @return operation
     */
    @NonNull
    @Override
    public BeanOperations getInternalBeanOperations(Object internalBean) {
        return internalBeanOperations;
    }
}
