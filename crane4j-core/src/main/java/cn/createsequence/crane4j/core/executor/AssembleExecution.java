package cn.createsequence.crane4j.core.executor;

import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.parser.AssembleOperation;
import cn.createsequence.crane4j.core.parser.BeanOperations;

import java.util.Collection;

/**
 * <p>表示某个装配操作一次执行，包含全部需要执行此操作的对象，以及用于执行的处理器。<br />
 * 该对象通常是一次性的，在{@link BeanOperationExecutor}的一次执行时创建，并在该次执行后销毁。
 *
 * @author huangchengxing
 * @see AssembleOperationHandler
 */
public interface AssembleExecution {

    /**
     * 获取操作对象对应的操作配置
     *
     * @return 操作配置
     */
    BeanOperations getBeanOperations();

    /**
     * 获取待操作对象的类型
     *
     * @return 类型
     */
    default Class<?> getTargetType() {
        return getBeanOperations().getTargetType();
    }

    /**
     * 获取待执行的装配操作
     *
     * @return 待执行的装配操作
     */
    AssembleOperation getOperation();

    /**
     * 获取装配操作的数据源容器
     *
     * @return 数据源容器
     */
    default Container<?> getContainer() {
        return getOperation().getContainer();
    }

    /**
     * 获取用于执行装配操作的处理器
     *
     * @return 装配操作处理器
     */
    default AssembleOperationHandler getHandler() {
        return getOperation().getAssembleOperationHandler();
    }

    /**
     * 获取待处理的目标对象
     *
     * @return 目标对象
     */
    Collection<Object> getTargets();
}
