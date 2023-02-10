package cn.createsequence.crane4j.core.support;

import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.createsequence.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.createsequence.crane4j.core.parser.BeanOperationParser;

/**
 * 框架全局配置类
 *
 * @author huangchengxing
 */
public interface Crane4jGlobalConfiguration {

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
     * 获取配置解析器
     *
     * @param parserType 配置解析器类型
     * @return 配置解析器
     */
    BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType);

    /**
     * 获取装配操作处理器
     *
     * @param handlerType 处理器类型
     * @return 装配操作处理器
     */
    AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType);

    /**
     * 获取拆卸操作处理器
     *
     * @param handlerType 处理器类型
     * @return 拆卸操作处理器
     */
    DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType);

}
