package cn.crane4j.core.executor.handler;

import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.support.NamedComponent;

import java.util.Collection;

/**
 * <p>Handler of disassembly operation.
 * Used to extract nested pending objects from a pending target object.
 *
 * @author huangchengxing
 * @see ReflectiveDisassembleOperationHandler
 */
public interface DisassembleOperationHandler extends NamedComponent {

    /**
     * Extract nested objects in object attributes according to disassembly configuration.
     *
     * @param operation disassembly operation to be performed
     * @param targets The target object to be processed should be the same type as {@code targetType}
     * @return nested objects
     */
    Collection<Object> process(DisassembleOperation operation, Collection<?> targets);
}
