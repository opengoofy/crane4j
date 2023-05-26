package cn.crane4j.core.support;

import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;

/**
 * Framework global configuration.
 *
 * @author huangchengxing
 */
public interface Crane4jGlobalConfiguration extends ContainerManager {

    /**
     * Get {@link ConverterManager}
     *
     * @return {@link ConverterManager}
     */
    ConverterManager getConverterManager();

    /**
     * Get property operator.
     *
     * @return property operator
     */
    PropertyOperator getPropertyOperator();

    /**
     * Get type handler.
     *
     * @return type handler
     */
    TypeResolver getTypeResolver();

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @return executor
     */
    BeanOperationExecutor getBeanOperationExecutor(String executorName);

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @return parser
     */
    BeanOperationParser getBeanOperationsParser(String parserName);

    /**
     * Get assemble operation handler.
     *
     * @param  handlerName handler name
     * @return handler
     */
    AssembleOperationHandler getAssembleOperationHandler(String handlerName);

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @return handler
     */
    DisassembleOperationHandler getDisassembleOperationHandler(String handlerName);
}
