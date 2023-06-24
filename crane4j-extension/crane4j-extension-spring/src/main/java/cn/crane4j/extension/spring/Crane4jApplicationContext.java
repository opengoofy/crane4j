package cn.crane4j.extension.spring;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.DefaultContainerManager;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

/**
 * <p>The global configuration class implemented based on the Spring context,
 * when obtaining the required components from the instance,
 * will directly obtain the corresponding bean through the {@link ApplicationContext} held.
 *
 * @author huangchengxing
 * @see ApplicationContext
 */
@Slf4j
@RequiredArgsConstructor
public class Crane4jApplicationContext extends DefaultContainerManager
    implements Crane4jGlobalConfiguration, SmartInitializingSingleton, DisposableBean, InitializingBean {

    static {
        // support compare by @Order annotation and Ordered interface
        Crane4jGlobalSorter.INSTANCE.addCompareValueExtractor(t -> {
            if (Objects.isNull(t)) {
                return null;
            }
            if (t instanceof  Ordered) {
                return ((Ordered)t).getOrder();
            }
            Integer order = findOrderFromAnnotation(t);
            if (Objects.isNull(order) && t instanceof DecoratingProxy) {
                order = findOrderFromAnnotation(((DecoratingProxy)t).getDecoratedClass());
            }
            return order;
        });
    }

    @Nullable
    private static Integer findOrderFromAnnotation(Object t) {
        if (!(t instanceof AnnotatedElement)) {
            return null;
        }
        Order annotation = AnnotatedElementUtils.findMergedAnnotation(((AnnotatedElement) t), Order.class);
        return Objects.nonNull(annotation) ? annotation.value() : null;
    }

    /**
     * application context
     */
    private final ApplicationContext applicationContext;

    /**
     * Get {@link ConverterManager}
     *
     * @return {@link ConverterManager}
     */
    @Override
    public ConverterManager getConverterManager() {
        return applicationContext.getBean(ConverterManager.class);
    }

    /**
     * Get property operator.
     *
     * @return property operator
     */
    @Override
    public PropertyOperator getPropertyOperator() {
        return applicationContext.getBean(PropertyOperator.class);
    }

    /**
     * Get type handler.
     *
     * @return type handler
     */
    @Override
    public TypeResolver getTypeResolver() {
        return applicationContext.getBean(TypeResolver.class);
    }

    /**
     * Get {@link ContainerProvider} by given name.
     *
     * @param name name
     * @return {@link ContainerProvider} instance
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends ContainerProvider> T getContainerProvider(String name) {
        T provider = super.getContainerProvider(name);
        return Objects.isNull(provider) && applicationContext.containsBean(name) ?
                (T)applicationContext.getBean(name, ContainerProvider.class) : provider;
    }

    /**
     * Obtaining and caching container instances from provider or definition.
     *
     * @param namespace namespace of container, which can also be the cache name for the container instance.
     * @return container instance
     * @see ContainerLifecycleProcessor#whenCreated
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <K> Container<K> getContainer(String namespace) {
        Container<K> container = super.getContainer(namespace);
        return Objects.isNull(container) && applicationContext.containsBean(namespace) ?
                applicationContext.getBean(namespace, Container.class) : container;
    }

    /**
     * Whether this provider has container of given {@code namespace}.
     *
     * @param namespace namespace
     * @return boolean
     */
    @Override
    public boolean containsContainer(String namespace) {
        return super.containsContainer(namespace) || applicationContext.containsBean(namespace);
    }

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @return executor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(String executorName) {
        return applicationContext.getBean(executorName, BeanOperationExecutor.class);
    }

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @return parser
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(String parserName) {
        return applicationContext.getBean(parserName, BeanOperationParser.class);
    }

    /**
     * Get assemble operation handler.
     *
     * @param  handlerName handler name
     * @return handler
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(String handlerName) {
        return applicationContext.getBean(handlerName, AssembleOperationHandler.class);
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @return handler
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(String handlerName) {
        return applicationContext.getBean(handlerName, DisassembleOperationHandler.class);
    }

    // ============================ life cycle ============================

    /**
     * Load all {@link ContainerLifecycleProcessor} from spring context.
     */
    @Override
    public void afterPropertiesSet() {
        applicationContext.getBeansOfType(ContainerLifecycleProcessor.class).forEach((name, processor) -> {
            log.info("install container register aware [{}]", name);
            registerContainerLifecycleProcessor(processor);
        });
    }

    /**
     * After Spring initializes all singleton beans,
     * register all beans that implement the {@link Container} interface with the current context.
     */
    @Override
    public void afterSingletonsInstantiated() {
        applicationContext.getBeansOfType(ContainerDefinition.class)
            .values().forEach(this::registerContainer);
        applicationContext.getBeansOfType(Container.class)
            .values().forEach(this::registerContainer);
    }

    /**
     * Clear container cache on destruction.
     */
    @Override
    public void destroy() {
        log.info("global configuration has been destroyed.");
        clear();
    }
}
