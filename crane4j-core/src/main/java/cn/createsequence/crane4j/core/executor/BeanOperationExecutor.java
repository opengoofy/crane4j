package cn.createsequence.crane4j.core.executor;

import cn.createsequence.crane4j.core.parser.BeanOperations;
import cn.createsequence.crane4j.core.parser.KeyTriggerOperation;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * 对象操作执行器，给定一组对象及其对应的操作配置，
 * 执行器将根据操作配置完成对全部对象的装配操作。
 *
 * @author huangchengxing
 * @see AbstractBeanOperationExecutor
 * @see AsyncBeanOperationExecutor
 * @see DisorderedBeanOperationExecutor
 * @see OrderedBeanOperationExecutor
 */
public interface BeanOperationExecutor {

    /**
     * 根据指定的{@link BeanOperations}完成对{@code targets}中所有对象的操作
     *
     * @param targets 目标对象
     * @param operations 待执行的操作
     * @param filter 操作过滤器，可以基于操作key、组别等属性过滤一些操作
     */
    void execute(Collection<?> targets, BeanOperations operations, Predicate<? super KeyTriggerOperation> filter);

    /**
     * 根据指定的{@link BeanOperations}完成对{@code targets}中所有对象的操作
     *
     * @param targets 目标对象
     * @param operations 待执行的操作
     */
    default void execute(Collection<?> targets, BeanOperations operations) {
        execute(targets, operations, t -> true);
    }
}
