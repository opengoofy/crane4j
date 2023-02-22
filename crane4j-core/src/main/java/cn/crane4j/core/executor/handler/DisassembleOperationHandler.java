package cn.crane4j.core.executor.handler;

import cn.crane4j.core.parser.DisassembleOperation;

import java.util.Collection;

/**
 * <p>Handler of disassembly operation.
 * Used to extract nested pending objects from a pending target object.
 *
 * @author huangchengxing
 * @see ReflectAssembleOperationHandler
 */
public interface DisassembleOperationHandler {

    /**
     * Extract nested objects in object attributes according to disassembly configuration.
     *
     * @param operation disassembly operation to be performed
     * @param targets The target object to be processed should be the same type as {@code targetType}
     */
    Collection<?> process(DisassembleOperation operation, Collection<?> targets);
}
