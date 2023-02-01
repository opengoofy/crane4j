package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.executor.BeanOperationExecutor;
import cn.createsequence.crane4j.core.support.Sorted;

import java.util.Collection;

/**
 * <p>对象操作配置，用于描述一个对象需要完成的装配/拆卸全部操作。<br />
 * 一般通过一个{@link Class}被{@link BeanOperationParser}解析得到，
 * 使用时与对应类型的对象集合一并通过{@link BeanOperationExecutor}完成全部操作的执行。
 *
 * <p>通过{@link #getAssembleOperations()}或{@link #getDisassembleOperations()}
 * 得到的操作配置一般是有序的，顺序遵循{@link Sorted#getSort()}定义。<br />
 * 但是实际执行顺序有{@link BeanOperationExecutor}保证，
 * 因此不同的执行器可能会导致实际上不同的执行顺序。
 *
 * <p>由于配置可能是嵌套的，比如一个对象中某个需要拆卸的属性类型为另一对象，
 * 因此当得到对象操作配置实例时可能仍然还在递归解析中，
 * 所以在使用前，需要通过{@link #isActive()}确保该配置对象已经完成解析。
 *
 * @author huangchengxing
 * @see AssembleOperation
 * @see DisassembleOperation
 * @see BeanOperationExecutor
 * @see BeanOperationParser
 */
public interface BeanOperations {

    /**
     * 获取当前操作对应的对象类型
     *
     * @return 对象类型
     */
    Class<?> getTargetType();

    /**
     * 获取装配操作
     *
     * @return 装配操作
     */
    Collection<AssembleOperation> getAssembleOperations();

    /**
     * 添加装配操作，若该操作已被添加，则会将其删除后再添加
     *
     * @param operation 装配操作
     */
    void putAssembleOperations(AssembleOperation operation);

    /**
     * 获取拆卸操作
     *
     * @return 拆卸操作
     */
    Collection<DisassembleOperation> getDisassembleOperations();

    /**
     * 添加拆卸操作，若该操作已被添加，则会将其删除后再添加
     *
     * @param operation 拆卸操作
     */
    void putDisassembleOperations(DisassembleOperation operation);

    /**
     * 当前操作配置是否已经就绪
     *
     * @return 是否
     */
    boolean isActive();

    /**
     * 设置当前操作配置就绪状态
     *
     * @param active 是否
     */
    void setActive(boolean active);
}
