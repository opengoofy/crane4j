package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.reflect.PropertyOperator;

/**
 * 框架全局配置类
 *
 * @author huangchengxing
 */
public interface Crane4jGlobalConfiguration {

    /**
     * 获取属性操作器
     *
     * @return 类型解析器
     */
    PropertyOperator getPropertyOperator();

    /**
     * 获取类型解析器
     *
     * @return 类型解析器
     */
    TypeResolver getTypeResolver();
    
    /**
     * 获取数据源容器
     *
     * @param namespace 命名空间
     * @return {@link Container}
     */
    Container<?> getContainer(String namespace);

    /**
     * 获取操作执行器
     *
     * @param executorType 执行器类型
     * @return cn.crane4j.core.executor.BeanOperationExecutor
     */
    BeanOperationExecutor getBeanOperationExecutor(Class<? extends BeanOperationExecutor> executorType);

    /**
     * 获取操作执行器
     *
     * @param executorName 执行器名称
     * @return cn.crane4j.core.executor.BeanOperationExecutor
     */
    BeanOperationExecutor getBeanOperationExecutor(String executorName);

    /**
     * 获取配置解析器
     *
     * @param parserType 配置解析器类型
     * @return 配置解析器
     */
    BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType);

    /**
     * 获取配置解析器
     *
     * @param parserName 配置解析器名称
     * @return 配置解析器
     */
    BeanOperationParser getBeanOperationsParser(String parserName);
    
    /**
     * 获取装配操作处理器
     *
     * @param handlerType 处理器类型
     * @return 装配操作处理器
     */
    AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType);

    /**
     * 获取装配操作处理器
     *
     * @param  handlerName 处理器器名称
     * @return 装配操作处理器
     */
    AssembleOperationHandler getAssembleOperationHandler(String handlerName);

    /**
     * 获取拆卸操作处理器
     *
     * @param handlerType 处理器类型
     * @return 拆卸操作处理器
     */
    DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType);

    /**
     * 获取拆卸操作处理器
     *
     * @param handlerName 处理器名称
     * @return 拆卸操作处理器
     */
    DisassembleOperationHandler getDisassembleOperationHandler(String handlerName);
}
