package cn.crane4j.core.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Simple implementation of {@link BeanOperations}.
 *
 * @author huangchengxing
 * @see SimpleBeanOperations
 */
@Getter
@RequiredArgsConstructor
public class SimpleBeanOperations implements BeanOperations {

    @Setter
    private boolean active = false;
    private final Class<?> targetType;
    private final List<AssembleOperation> assembleOperations = new ArrayList<>();
    private final List<DisassembleOperation> disassembleOperations = new ArrayList<>();

    /**
     * Add assembly operation. <br />
     * If the operation has been added, it will be deleted before adding.
     *
     * @param operation operation
     */
    @Override
    public void putAssembleOperations(AssembleOperation operation) {
        Objects.requireNonNull(operation);
        assembleOperations.remove(operation);
        assembleOperations.add(operation);
    }

    /**
     * Add disassembly operation. <br />
     * If the operation has been added, it will be deleted before adding.
     *
     * @param operation operation
     */
    @Override
    public void putDisassembleOperations(DisassembleOperation operation) {
        Objects.requireNonNull(operation);
        disassembleOperations.remove(operation);
        disassembleOperations.add(operation);
    }
}
