package cn.crane4j.springboot.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.lang.Assert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>基于Spring上下文实现的全局配置类，当从实例获取所需组件时，
 * 将会直接通过注入的{@link ApplicationContext}获取对应的Bean。
 *
 * @author huangchengxing
 * @see ApplicationContext
 */
@Slf4j
@RequiredArgsConstructor
public class Crane4jApplicationContext
    implements Crane4jGlobalConfiguration, SmartInitializingSingleton, DisposableBean {

    /**
     * Spring上下文
     */
    private final ApplicationContext applicationContext;

    /**
     * 注册的数据源容器
     */
    @Getter
    private final Map<String, Container<?>> registeredContainers = new ConcurrentHashMap<>();

    /**
     * 获取属性操作器
     *
     * @return 类型解析器
     */
    @Override
    public PropertyOperator getPropertyOperator() {
        return applicationContext.getBean(PropertyOperator.class);
    }

    /**
     * 获取类型解析器
     *
     * @return 类型解析器
     */
    @Override
    public TypeResolver getTypeResolver() {
        return applicationContext.getBean(TypeResolver.class);
    }

    /**
     * 获取数据源容器
     *
     * @param namespace 命名空间
     * @return {@link Container}
     */
    @Override
    public Container<?> getContainer(String namespace) {
        Container<?> container = registeredContainers.get(namespace);
        if (Objects.isNull(container)) {
            container = applicationContext.getBean(namespace, Container.class);
        }
        return Objects.requireNonNull(container, () -> "cannot find container [" + namespace + "]");
    }

    /**
     * 获取操作执行器
     *
     * @param executorType 执行器类型
     * @return cn.crane4j.core.executor.BeanOperationExecutor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(Class<? extends BeanOperationExecutor> executorType) {
        return applicationContext.getBean(executorType);
    }

    /**
     * 获取操作执行器
     *
     * @param executorName 执行器名称
     * @return cn.crane4j.core.executor.BeanOperationExecutor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(String executorName) {
        return applicationContext.getBean(executorName, BeanOperationExecutor.class);
    }

    /**
     * 获取配置解析器
     *
     * @param parserType 配置解析器类型
     * @return 配置解析器
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType) {
        return applicationContext.getBean(parserType);
    }

    /**
     * 获取配置解析器
     *
     * @param parserName 配置解析器名称
     * @return 配置解析器
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(String parserName) {
        return applicationContext.getBean(parserName, BeanOperationParser.class);
    }

    /**
     * 获取装配操作处理器
     *
     * @param handlerType 处理器类型
     * @return 装配操作处理器
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType) {
        return applicationContext.getBean(handlerType);
    }

    /**
     * 获取装配操作处理器
     *
     * @param handlerName 处理器器名称
     * @return 装配操作处理器
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(String handlerName) {
        return applicationContext.getBean(handlerName, AssembleOperationHandler.class);
    }

    /**
     * 获取拆卸操作处理器
     *
     * @param handlerType 处理器类型
     * @return 拆卸操作处理器
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType) {
        return applicationContext.getBean(handlerType);
    }

    /**
     * 获取拆卸操作处理器
     *
     * @param handlerName 处理器名称
     * @return 拆卸操作处理器
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(String handlerName) {
        return applicationContext.getBean(handlerName, DisassembleOperationHandler.class);
    }

    /**
     * 注册数据源容器
     *
     * @param container 要注册的容器
     * @throws Crane4jException 当该容器的命名空间已被注册时抛出
     */
    public void registerContainer(Container<?> container) {
        String namespace = container.getNamespace();
        Container<?> old = registeredContainers.put(namespace, container);
        Assert.isNull(old, () -> new Crane4jException("the container [{}] has been registered", namespace));
        log.info("register data source container [{}]", container.getNamespace());
    }

    /**
     * 当Spring初始化所有单例Bean后，将所有实现了{@link Container}接口的Bean都注册到当前上下文
     */
    @Override
    public void afterSingletonsInstantiated() {
        applicationContext.getBeansOfType(Container.class)
            .values().forEach(this::registerContainer);
    }

    /**
     * 销毁时清空容器缓存
     */
    @Override
    public void destroy() {
        log.info("global configuration has been destroyed.");
        registeredContainers.clear();
    }
}
