package cn.crane4j.core.parser;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.support.Sorted;

import java.util.Collection;

/**
 * <p>Object operation configuration is used to describe all assembly and disassembly operations of an object.<br />
 * Generally, a {@link Class} is parsed by {@link BeanOperationParser}, and when used,
 * all operations are performed through {@link BeanOperationExecutor} with the corresponding type of object collection.
 *
 * <p>Through {@link #getAssembleOperations()} or {@link #getDisassembleOperations()} obtained operation configuration
 * is generally orderly, and the order follows the definition of {@link Sorted#getSort()}.<br />
 * However, the actual execution order needs to be guaranteed by {@link BeanOperationExecutor},
 * so different executors may lead to different execution orders.
 *
 * <p>Because the configuration may be nested, for example,
 * an attribute type that needs to be disassembled in an object is another object,
 * so when the object operation configuration instance is obtained, it may still be in recursive resolution,
 * so before use, you need to ensure that the configuration object
 * has completed resolution through {@link #isActive()}.
 *
 * @author huangchengxing
 * @see AssembleOperation
 * @see DisassembleOperation
 * @see BeanOperationExecutor
 * @see BeanOperationParser
 */
public interface BeanOperations {

    /**
     * Get the object type corresponding to the current operation.
     *
     * @return type
     */
    Class<?> getTargetType();

    /**
     * Get assembly operations.
     *
     * @return operations
     */
    Collection<AssembleOperation> getAssembleOperations();

    /**
     * Add assembly operation. <br />
     * If the operation has been added, it will be deleted before adding.
     *
     * @param operation operation
     */
    void putAssembleOperations(AssembleOperation operation);

    /**
     * Get disassembly operations.
     *
     * @return operations
     */
    Collection<DisassembleOperation> getDisassembleOperations();

    /**
     * Add disassembly operation. <br />
     * If the operation has been added, it will be deleted before adding.
     *
     * @param operation operation
     */
    void putDisassembleOperations(DisassembleOperation operation);

    /**
     * Whether the current operation configuration is active.
     *
     * @return true if configuration is active, false otherwise
     */
    boolean isActive();

    /**
     * Set the current operation configuration active state.
     *
     * @param active active state
     */
    void setActive(boolean active);
}
