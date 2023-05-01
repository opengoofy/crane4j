package cn.crane4j.extension.spring.operator;

import cn.crane4j.annotation.Operator;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.StringUtils;
import cn.crane4j.extension.spring.scanner.ClassScanner;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Registrar for proxy object bean of operator interfaces.
 *
 * @author huangchengxing
 * @see OperatorProxyFactory
 * @see Operator
 * @see OperatorScan
 * @since 1.3.0
 */
@Slf4j
public class OperatorBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
            importingClassMetadata.getAnnotationAttributes(OperatorScan.class.getName())
        );
        if (annotationAttributes != null) {
            doRegisterBeanDefinitions(annotationAttributes, registry);
        }
    }

    private void doRegisterBeanDefinitions(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        ClassScanner classScanner = new ClassScanner();
        Set<Class<?>> operatorTypes = Stream.of(annotationAttributes.getStringArray("scan"))
            .filter(StringUtils::isNotEmpty)
            .map(classScanner::scan)
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(Collection::stream)
            .filter(Class::isInterface)
            .filter(operatorType -> AnnotatedElementUtils.isAnnotated(operatorType, Operator.class))
            .collect(Collectors.toSet());
        CollectionUtils.addAll(operatorTypes, annotationAttributes.getClassArray("includes"));
        Arrays.asList(annotationAttributes.getClassArray("excludes"))
            .forEach(operatorTypes::remove);
        for (Class<?> operatorType : operatorTypes) {
            registerOperatorBeanDefinition(registry, operatorType);
        }
    }

    private static void registerOperatorBeanDefinition(BeanDefinitionRegistry registry, Class<?> operatorType) {
        log.debug("register operator bean definition for [{}]", operatorType);
        // register factory bean
        BeanDefinition factoryBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(OperatorProxyFactoryBean.class)
            .addAutowiredProperty("operatorProxyFactory")
            .addPropertyValue("operatorType", operatorType)
            .setAutowireMode(2)
            .getBeanDefinition();
        String factoryBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(factoryBeanDefinition, registry);
        factoryBeanName += "#" + operatorType.getName();
        registry.registerBeanDefinition(factoryBeanName, factoryBeanDefinition);

        // register operator bean
        BeanDefinition operatorBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(operatorType)
            .setFactoryMethodOnBean("getObject", factoryBeanName)
            .setLazyInit(true)
            .getBeanDefinition();
        String operatorBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(operatorBeanDefinition, registry);
        registry.registerBeanDefinition(operatorBeanName, operatorBeanDefinition);
    }

    /**
     * {@link FactoryBean} of operator interface proxy object.
     *
     * @author huangchengxing
     */
    @Setter
    public static class OperatorProxyFactoryBean<T> implements FactoryBean<T> {

        private OperatorProxyFactory operatorProxyFactory;
        private Class<T> operatorType;

        @Override
        public T getObject() {
            return operatorProxyFactory.get(operatorType);
        }

        @Override
        public Class<?> getObjectType() {
            return operatorType;
        }
    }
}
