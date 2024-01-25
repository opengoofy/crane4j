package cn.crane4j.extension.spring;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.container.ContainerMethodAnnotationProcessor;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.util.Asserts;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * <p>Post process the bean, scan the method with
 * {@link ContainerMethod} annotation in the class or method of class,
 * and adapt it to {@link Container} instance
 * according to {@link MethodContainerFactory} registered in the Spring context.
 *
 * @author huangchengxing
 * @see ContainerMethod
 * @see MethodContainerFactory
 * @see Crane4jGlobalConfiguration
 */
@Order
@Slf4j
public class BeanMethodContainerRegistrar
    extends ContainerMethodAnnotationProcessor
    implements InitializingBean, SmartInitializingSingleton, ApplicationContextAware, DisposableBean {

    @Setter
    private ApplicationContext applicationContext;
    private final Crane4jGlobalConfiguration configuration;

    /**
     * Create an {@link BeanMethodContainerRegistrar} instance.
     *
     * @param annotationFinder annotation finder
     * @param configuration crane4j global configuration
     */
    public BeanMethodContainerRegistrar(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        super(Collections.emptyList(), annotationFinder);
        this.configuration = configuration;
    }

    /**
     * Clear resources when destroying beans
     */
    @Override
    public void destroy() {
        nonAnnotatedClasses.clear();
    }

    /**
     * <p>register method containers which scanned from the bean methods.
     *
     * @param target target
     * @param targetType target type
     * @see #process
     * @since 2.5.0
     */
    public void register(Object target, Class<?> targetType) {
        register(target, targetType, null);
    }

    /**
     * <p>register method containers which scanned from the bean methods.
     *
     * @param target target
     * @param targetType target type
     * @param customizer container customizer
     * @see #process
     * @since 2.5.0
     */
    public void register(
        Object target, Class<?> targetType, @Nullable UnaryOperator<Container<Object>> customizer) {
        Asserts.isNotNull(applicationContext, "applicationContext must not be null");
        Collection<Container<Object>> containers = process(target, targetType);
        log.debug("process [{}] annotated methods for bean [{}]", containers.size(), target);
        customizer = Objects.isNull(customizer) ? UnaryOperator.identity() : customizer;
        containers.stream()
            .map(customizer)
            .forEach(configuration::registerContainer);
    }

    @Override
    public void afterPropertiesSet() {
        Asserts.isNotNull(applicationContext, "applicationContext must not be null");
        Asserts.isTrue(
            applicationContext.getAutowireCapableBeanFactory() instanceof ConfigurableListableBeanFactory,
            "applicationContext must have a ConfigurableListableBeanFactory"
        );
        Map<String, MethodContainerFactory> containerFactories = applicationContext.getBeansOfType(MethodContainerFactory.class);
        containerFactories.forEach((n, f) -> {
            log.info("register method container factory [{}] with name [{}]", f.getClass().getName(), n);
            registerMethodContainerFactory(f);
        });
    }

    /**
     * <p>Scan and process the method with the specified annotation in the class.
     * If the annotation also exists in the class, find and process the corresponding method in the class.
     */
    @Override
    public void afterSingletonsInstantiated() {
        ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
        String[] beanNames = beanFactory.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            Class<?> beanType = determineTargetType(beanFactory, beanName);
            if (Objects.isNull(beanType)) {
                continue;
            }
            Object bean = beanFactory.getBean(beanName);
            Collection<Container<Object>> containers = process(bean, beanType);
            log.debug("process [{}] annotated methods for bean [{}]", containers.size(), beanName);
            containers.forEach(configuration::registerContainer);
        }
        nonAnnotatedClasses.clear();
    }

    @SuppressWarnings("all")
    @Nullable
    private Class<?> determineTargetType(ConfigurableListableBeanFactory beanFactory, String beanName) {
        try {
            Class<?> type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
            if (ScopedObject.class.isAssignableFrom(type)) {
                Class<?> targetClass = AutoProxyUtils.determineTargetClass(
                    beanFactory, ScopedProxyUtils.getTargetBeanName(beanName)
                );
            }
            return type;
        }
        catch (Throwable ex) {
            log.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
        }
        return null;
    }
}
