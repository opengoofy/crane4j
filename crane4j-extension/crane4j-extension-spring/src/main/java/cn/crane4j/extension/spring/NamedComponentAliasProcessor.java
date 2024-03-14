package cn.crane4j.extension.spring;

import cn.crane4j.core.support.NamedComponent;
import cn.crane4j.core.util.Asserts;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A {@link BeanPostProcessor} implementation that registers named components as aliases in the Spring IoC container.
 *
 * @author huangchengxing
 * @see NamedComponent
 */
@Slf4j
public class NamedComponentAliasProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof NamedComponent) {
            String name = ((NamedComponent) bean).getName();
            if (!configurableListableBeanFactory.containsBean(name)) {
                configurableListableBeanFactory.registerAlias(beanName, name);
                log.debug("Register named component alias: {} -> {}", name, beanName);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        Asserts.isTrue(beanFactory instanceof ConfigurableListableBeanFactory, "The bean factory must be an instance of ConfigurableListableBeanFactory");
        this.configurableListableBeanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }
}
