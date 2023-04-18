package cn.crane4j.core.parser;

import lombok.Getter;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.List;

/**
 * Empty bean operations.
 *
 * @author huangchengxing
 */
@Getter
public class EmptyBeanOperations implements BeanOperations {

    public static final EmptyBeanOperations INSTANCE = new EmptyBeanOperations();

    private final boolean active = true;
    private final boolean empty = true;
    private final AnnotatedElement source = Object.class;
    private final List<AssembleOperation> assembleOperations = Collections.emptyList();
    private final List<DisassembleOperation> disassembleOperations = Collections.emptyList();

    /**
     * Add assembly operation. <br />
     * If the operation has been added, it will be deleted before adding.
     *
     * @param operation operation
     */
    @Override
    public void addAssembleOperations(AssembleOperation operation) {
        throw new UnsupportedOperationException("empty bean operation unsupported add assemble operations");
    }

    /**
     * Add disassembly operation. <br />
     * If the operation has been added, it will be deleted before adding.
     *
     * @param operation operation
     */
    @Override
    public void addDisassembleOperations(DisassembleOperation operation) {
        throw new UnsupportedOperationException("empty bean operation unsupported add disassemble operations");
    }

    /**
     * Set the current operation configuration active state.
     *
     * @param active active state
     */
    @Override
    public void setActive(boolean active) {
        // do nothing
    }
}
