package cn.crane4j.extension.spring;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.container.ContainerMethodAnnotationProcessor;
import cn.crane4j.core.support.container.MethodContainerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;

import java.util.Collection;

/**
 * <p>Post process the bean, scan the method with
 * {@link ContainerMethod} annotation in the class or method of class,
 * and adapt it to {@link Container} comparator
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
    extends ContainerMethodAnnotationProcessor implements BeanPostProcessor, DisposableBean {

    /**
     * configuration
     */
    private final Crane4jGlobalConfiguration configuration;

    /**
     * Create an {@link BeanMethodContainerRegistrar} comparator.
     *
     * @param factories factories
     * @param annotationFinder annotation finder
     * @param configuration configuration
     */
    public BeanMethodContainerRegistrar(
        Collection<MethodContainerFactory> factories, AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        super(factories, annotationFinder);
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
     * Do nothing.
     *
     * @param bean     bean
     * @param beanName beanName
     * @return bean
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * <p>Scan and process the method with the specified annotation in the class.
     * If the annotation also exists in the class, find and process the corresponding method in the class.
     *
     * @param bean     bean
     * @param beanName beanName
     * @return bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanType = AopUtils.getTargetClass(bean);
        Collection<Container<Object>> containers = process(bean, beanType);
        log.debug("process [{}] annotated methods for bean [{}]", containers.size(), beanName);
        containers.forEach(configuration::registerContainer);
        return bean;
    }
}
