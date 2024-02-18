package cn.crane4j.core.parser.operation;

import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * <p>The {@link DisassembleOperation} implementation that express the disassemble operation of data from a fixed type.
 *
 * @author huangchengxing
 */
@SuperBuilder
public class TypeFixedDisassembleOperation extends SimpleKeyTriggerOperation implements DisassembleOperation {

    @Getter
    private final Class<?> sourceType;
    @Getter
    private final DisassembleOperationHandler disassembleOperationHandler;
    private final BeanOperations internalBeanOperations;

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
