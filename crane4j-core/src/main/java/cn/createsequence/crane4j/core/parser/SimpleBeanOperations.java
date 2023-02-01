package cn.createsequence.crane4j.core.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link BeanOperations}的简单实现
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
     * 添加装配操作，若该操作已被添加，则会将其删除后再添加
     *
     * @param operation 装配操作
     */
    @Override
    public void putAssembleOperations(AssembleOperation operation) {
        Objects.requireNonNull(operation);
        assembleOperations.remove(operation);
        assembleOperations.add(operation);
    }

    /**
     * 添加拆卸操作，若该操作已被添加，则会将其删除后再添加
     *
     * @param operation 拆卸操作
     */
    @Override
    public void putDisassembleOperations(DisassembleOperation operation) {
        Objects.requireNonNull(operation);
        disassembleOperations.remove(operation);
        disassembleOperations.add(operation);
    }
}
