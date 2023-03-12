package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.reflect.PropertyOperator;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

/**
 * Framework global configuration.
 *
 * @author huangchengxing
 */
public interface Crane4jGlobalConfiguration extends ContainerProvider {

    /**
     * Whether the container has been registered.
     *
     * @param namespace namespace
     * @return boolean
     */
    boolean containsContainer(String namespace);

    /**
     * Replace the registered container.
     * <ul>
     *     <li>if the container is not registered, it will be added;</li>
     *     <li>if {@code replacer} return {@code null}, the old container will be deleted;</li>
     * </ul>
     *
     * @param namespace namespace
     * @param replacer replacer
     * @return old container
     */
    @Nullable
    Container<?> replaceContainer(String namespace, UnaryOperator<Container<?>> replacer);

    /**
     * Get property operator.
     *
     * @return property operator
     */
    PropertyOperator getPropertyOperator();

    /**
     * Get type resolver.
     *
     * @return type resolver
     */
    TypeResolver getTypeResolver();

    /**
     * Get container provider.
     *
     * @param providerType provider type
     * @return provider
     */
    ContainerProvider getContainerProvider(Class<? extends ContainerProvider> providerType);

    /**
     * Get container provider.
     *
     * @param providerName provider name
     * @return provider
     */
    ContainerProvider getContainerProvider(String providerName);
    
    /**
     * Get bean operation executor.
     *
     * @param executorType executor type
     * @return executor
     */
    BeanOperationExecutor getBeanOperationExecutor(Class<? extends BeanOperationExecutor> executorType);

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
     * @param parserType parser type
     * @return parser
     */
    BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType);

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
     * @param handlerType handler type
     * @return handler
     */
    AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType);

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
     * @param handlerType handler type
     * @return handler
     */
    DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType);

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @return handler
     */
    DisassembleOperationHandler getDisassembleOperationHandler(String handlerName);
}
