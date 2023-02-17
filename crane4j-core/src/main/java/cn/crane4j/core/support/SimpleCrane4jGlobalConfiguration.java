package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.lang.Assert;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Crane4jGlobalConfiguration}的基本实现
 *
 * @author huangchengxing
 */
@Getter
public class SimpleCrane4jGlobalConfiguration implements Crane4jGlobalConfiguration {

    @Setter
    private TypeResolver typeResolver;
    @Setter
    private PropertyOperator propertyOperator;
    private final Map<String, Container<?>> containerMap = new HashMap<>(16);
    private final Map<String, BeanOperationParser> beanOperationParserMap = new HashMap<>(16);
    private final Map<String, AssembleOperationHandler> assembleOperationHandlerMap = new HashMap<>(4);
    private final Map<String, DisassembleOperationHandler> disassembleOperationHandlerMap = new HashMap<>(4);
    private final Map<String, BeanOperationExecutor> beanOperationExecutorMap = new HashMap<>(4);

    /**
     * 获取数据源容器
     *
     * @param namespace 命名空间
     * @return {@link Container}
     */
    @Override
    public Container<?> getContainer(String namespace) {
        return containerMap.get(namespace);
    }

    /**
     * 获取操作执行器
     *
     * @param executorType 执行器类型
     * @return cn.crane4j.core.executor.BeanOperationExecutor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(Class<? extends BeanOperationExecutor> executorType) {
        return getBeanOperationExecutor(executorType.getName());
    }

    /**
     * 获取操作执行器
     *
     * @param executorName 执行器名称
     * @return cn.crane4j.core.executor.BeanOperationExecutor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(String executorName) {
        BeanOperationExecutor executor = beanOperationExecutorMap.get(executorName);
        Assert.notNull(executor, () -> new Crane4jException("cannot find executor [{}]", executorName));
        return executor;
    }

    /**
     * 获取配置解析器
     *
     * @param parserType 配置解析器类型
     * @return 配置解析器
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType) {
        return getBeanOperationsParser(parserType.getName());
    }

    /**
     * 获取配置解析器
     *
     * @param parserName 配置解析器名称
     * @return 配置解析器
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(String parserName) {
        BeanOperationParser parser = beanOperationParserMap.get(parserName);
        Assert.notNull(parser, () -> new Crane4jException("cannot find parser [{}]", parserName));
        return parser;
    }

    /**
     * 获取装配操作处理器
     *
     * @param handlerType 处理器类型
     * @return 装配操作处理器
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType) {
        return getAssembleOperationHandler(handlerType.getName());
    }

    /**
     * 获取装配操作处理器
     *
     * @param handlerName 处理器器名称
     * @return 装配操作处理器
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(String handlerName) {
        AssembleOperationHandler handler = assembleOperationHandlerMap.get(handlerName);
        Assert.notNull(handler, () -> new Crane4jException("cannot find handler [{}]", handlerName));
        return handler;
    }

    /**
     * 获取拆卸操作处理器
     *
     * @param handlerType 处理器类型
     * @return 拆卸操作处理器
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType) {
        return getDisassembleOperationHandler(handlerType.getName());
    }

    /**
     * 获取拆卸操作处理器
     *
     * @param handlerName 处理器名称
     * @return 拆卸操作处理器
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(String handlerName) {
        DisassembleOperationHandler handler = disassembleOperationHandlerMap.get(handlerName);
        Assert.notNull(handler, () -> new Crane4jException("cannot find handler [{}]", handlerName));
        return handler;
    }
}
