package cn.crane4j.extension.spring.scanner;

import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.spring.annotation.ContainerConstantScan;
import cn.crane4j.extension.spring.annotation.ContainerEnumScan;
import cn.crane4j.extension.spring.util.ContainerResolveUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringValueResolver;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <p>A registrar for container which scanned by {@link ContainerConstantScan} and {@link ContainerEnumScan}.
 * 
 * <p>When application context is ready, it will register a bean definition
 * of {@link ScannedContainerConfiguration} to spring container for recording scanned container,
 * then, the scanned container which recorded by {@link ScannedContainerConfiguration} will be registered to {@link Crane4jGlobalConfiguration}
 * when spring invoke {@link InitializingBean#afterPropertiesSet()} method of the {@link ScannedContainerRegistrar} bean.
 *
 * @author huangchengxing
 * @see ContainerConstantScan
 * @see ContainerEnumScan
 * @see ScannedContainerConfiguration
 * @since 2.1.0
 */
@Slf4j
public class ScannedContainerRegistrar
    implements ImportBeanDefinitionRegistrar, InitializingBean, EmbeddedValueResolverAware {

    private static final String SCANNED_CONTAINER_CONFIGURATION_BEAN_NAME = "internalScannedContainerConfigurationBeanName";

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private AnnotationFinder annotationFinder;
    @Autowired
    private PropertyOperator propertyOperator;
    @Autowired
    private Crane4jGlobalConfiguration configuration;
    @Autowired
    private ClassScanner classScanner;
    @Setter
    private StringValueResolver embeddedValueResolver;

    /**
     * Register bean definitions as necessary based on the given annotation metadata of
     * the importing {@code @Configuration} class.
     * <p>Note that {@link BeanDefinitionRegistryPostProcessor} types may <em>not</em> be
     * registered here, due to lifecycle constraints related to {@code @Configuration}
     * class processing.
     * <p>The default implementation is empty.
     *
     * @param importingClassMetadata annotation metadata of the importing class
     * @param registry               current bean definition registry
     */
    @Override
    public void registerBeanDefinitions(
        @NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(ScannedContainerConfiguration.class)
            .addPropertyValue("constantContainerScan", importingClassMetadata.getAnnotationAttributes(ContainerConstantScan.class.getName()))
            .addPropertyValue("enumContainerScan", importingClassMetadata.getAnnotationAttributes(ContainerEnumScan.class.getName()))
            .setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_NO)
            .getBeanDefinition();
        if (!registry.containsBeanDefinition(SCANNED_CONTAINER_CONFIGURATION_BEAN_NAME)) {
            log.info("load annotation metadata from @ContainerConstantScan or @ContainerEnumScan");
            registry.registerBeanDefinition(SCANNED_CONTAINER_CONFIGURATION_BEAN_NAME, definition);
        }
    }

    /**
     * Load containers which resolve from configuration of annotation metadata.
     */
    @Override
    public void afterPropertiesSet() {
        if (!applicationContext.containsBeanDefinition(SCANNED_CONTAINER_CONFIGURATION_BEAN_NAME)) {
            return;
        }
        ScannedContainerConfiguration scannedContainerConfiguration = applicationContext.getBean(
            SCANNED_CONTAINER_CONFIGURATION_BEAN_NAME, ScannedContainerConfiguration.class
        );
        log.info("register container which resolve from @ContainerConstantScan or @ContainerEnumScan");
        Optional.ofNullable(scannedContainerConfiguration.getConstantContainerScan())
            .map(attributes -> ContainerResolveUtils.resolveComponentTypesFromMetadata(attributes, classScanner, embeddedValueResolver))
            .filter(CollectionUtils::isNotEmpty)
            .ifPresent(types -> ContainerResolveUtils.loadConstantClass(types, configuration, annotationFinder));
        AnnotationAttributes enumContainerScan = scannedContainerConfiguration.getEnumContainerScan();
        if (Objects.nonNull(enumContainerScan)) {
            Set<Class<?>> types = ContainerResolveUtils.resolveComponentTypesFromMetadata(
                enumContainerScan, classScanner, embeddedValueResolver
            );
            if (CollectionUtils.isNotEmpty(types)) {
                ContainerResolveUtils.loadContainerEnum(
                    types, enumContainerScan.getBoolean("isOnlyLoadAnnotatedEnum"),
                    configuration, annotationFinder, propertyOperator
                );
            }
        }
    }

    @Setter
    @Getter
    protected static class ScannedContainerConfiguration {
        @Nullable
        private AnnotationAttributes constantContainerScan;
        @Nullable
        private AnnotationAttributes enumContainerScan;
    }
}
