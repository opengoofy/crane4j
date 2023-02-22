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
 * Basic implementation of {@link Crane4jGlobalConfiguration}.
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
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     */
    @Override
    public Container<?> getContainer(String namespace) {
        return containerMap.get(namespace);
    }

    /**
     * Get bean operation executor.
     *
     * @param executorType executor type
     * @return executor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(Class<? extends BeanOperationExecutor> executorType) {
        return getBeanOperationExecutor(executorType.getName());
    }

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @return executor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(String executorName) {
        BeanOperationExecutor executor = beanOperationExecutorMap.get(executorName);
        Assert.notNull(executor, () -> new Crane4jException("cannot find executor [{}]", executorName));
        return executor;
    }

    /**
     * Get bean operation parser.
     *
     * @param parserType parser type
     * @return parser
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType) {
        return getBeanOperationsParser(parserType.getName());
    }

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @return parser
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(String parserName) {
        BeanOperationParser parser = beanOperationParserMap.get(parserName);
        Assert.notNull(parser, () -> new Crane4jException("cannot find parser [{}]", parserName));
        return parser;
    }

    /**
     * Get assemble operation handler.
     *
     * @param handlerType handler type
     * @return handler
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType) {
        return getAssembleOperationHandler(handlerType.getName());
    }

    /**
     * Get assemble operation handler.
     *
     * @param  handlerName handler name
     * @return handler
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(String handlerName) {
        AssembleOperationHandler handler = assembleOperationHandlerMap.get(handlerName);
        Assert.notNull(handler, () -> new Crane4jException("cannot find handler [{}]", handlerName));
        return handler;
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerType handler type
     * @return handler
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType) {
        return getDisassembleOperationHandler(handlerType.getName());
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @return handler
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(String handlerName) {
        DisassembleOperationHandler handler = disassembleOperationHandlerMap.get(handlerName);
        Assert.notNull(handler, () -> new Crane4jException("cannot find handler [{}]", handlerName));
        return handler;
    }
}
