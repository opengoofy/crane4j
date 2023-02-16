package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;

import java.util.Collection;

/**
 * <p>装配操作处理器。<br />
 * 输入装配操作，与对应类型的待处理对象，执行器将根据配置完成如下操作：
 * <ul>
 *     <li>提取key值；</li>
 *     <li>将key值转换为对应的数据源对象；</li>
 *     <li>完成数据源对象属性与待处理对象属性建的映射；</li>
 * </ul>
 * 出于性能考虑，实现类需要尽可能减少对Bean的读写以及对数据源容器的请求。
 *
 * @author huangchengxing
 * @see ReflectAssembleOperationHandler
 */
public interface AssembleOperationHandler {

    /**
     * 执行装配操作
     *
     * @param container 数据源容器
     * @param executions 待执行的装配操作
     */
    void process(Container<?> container, Collection<AssembleExecution> executions);
}
