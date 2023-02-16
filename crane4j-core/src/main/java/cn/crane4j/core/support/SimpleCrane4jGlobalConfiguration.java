package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
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
    private final Map<String, Container<?>> containerMap = new HashMap<>(16);
    private final Map<Class<? extends BeanOperationParser>, BeanOperationParser> beanOperationParserMap = new HashMap<>(16);
    private final Map<Class<? extends AssembleOperationHandler>, AssembleOperationHandler> assembleOperationHandlerMap = new HashMap<>(4);
    private final Map<Class<? extends DisassembleOperationHandler>, DisassembleOperationHandler> disassembleOperationHandlerMap = new HashMap<>(4);


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
     * 获取配置解析器
     *
     * @param parserType 配置解析器类型
     * @return 配置解析器
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType) {
        return beanOperationParserMap.get(parserType);
    }

    /**
     * 获取装配操作处理器
     *
     * @param handlerType 处理器类型
     * @return 装配操作处理器
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType) {
        return assembleOperationHandlerMap.get(handlerType);
    }

    /**
     * 获取拆卸操作处理器
     *
     * @param handlerType 处理器类型
     * @return 拆卸操作处理器
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType) {
        return disassembleOperationHandlerMap.get(handlerType);
    }
}
