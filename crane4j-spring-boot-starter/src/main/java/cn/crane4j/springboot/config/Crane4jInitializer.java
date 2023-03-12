package cn.crane4j.springboot.config;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.cache.Cache;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.springboot.support.Crane4jApplicationContext;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ClassUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The default initializer is used to initialize some caches or components after the application is started.
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class Crane4jInitializer implements ApplicationRunner {

    private final MetadataReaderFactory readerFactory;
    private final ResourcePatternResolver resolver;

    private final CacheManager cacheManager;
    private final Crane4jProperties crane4jProperties;
    private final AnnotationFinder annotationFinder;
    private final Crane4jApplicationContext configuration;
    private final Collection<BeanOperationParser> parsers;

    @SneakyThrows
    @Override
    public void run(ApplicationArguments args) {
        log.info("start initializing component cache......");
        // load enumeration and register it as a container
        loadContainerEnum();
        // load a constant class and register it as a container
        loadConstantClass();
        // pre resolution class operation configuration
        loadOperateEntity();
        // replace to cacheable container
        wrapCacheableContainer();
    }

    private void loadConstantClass() {
        Set<String> constantPackages = crane4jProperties.getContainerConstantPackages();
        constantPackages.forEach(path -> readMetadata(path, reader -> {
            Class<?> targetType = ClassUtil.loadClass(reader.getClassMetadata().getClassName());
            if (AnnotatedElementUtils.isAnnotated(targetType, ContainerConstant.class)) {
                Container<String> container = ConstantContainer.forConstantClass(targetType, annotationFinder);
                configuration.registerContainer(container);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private void wrapCacheableContainer() {
        crane4jProperties.getCacheContainers().forEach((cacheName, namespaces) -> {
            Cache<Object> cache = cacheManager.getCache(cacheName);
            namespaces.forEach(namespace -> configuration.replaceContainer(
                namespace, container -> Objects.isNull(container) ? null : new CacheableContainer<>((Container<Object>)container, cache)
            ));
        });
    }

    @SuppressWarnings("unchecked")
    private void loadContainerEnum() {
        Set<String> enumPackages = crane4jProperties.getContainerEnumPackages();
        enumPackages.forEach(path -> readMetadata(path, reader -> {
            Class<?> targetType = ClassUtil.loadClass(reader.getClassMetadata().getClassName());
            boolean supported = targetType.isEnum()
                && (!crane4jProperties.isOnlyLoadAnnotatedEnum() || AnnotatedElementUtils.isAnnotated(targetType, ContainerEnum.class));
            if (supported) {
                Container<Enum<?>> container = ConstantContainer.forEnum((Class<Enum<?>>)targetType, annotationFinder);
                configuration.registerContainer(container);
            }
        }));
    }

    private void loadOperateEntity() {
        Set<String> entityPackages = crane4jProperties.getOperateEntityPackages();
        entityPackages.forEach(path -> readMetadata(path, reader -> {
            Class<?> targetType = ClassUtil.loadClass(reader.getClassMetadata()
                .getClassName());
            parsers.forEach(parser -> parser.parse(targetType));
        }));
    }

    @SneakyThrows
    private void readMetadata(String path, Consumer<MetadataReader> consumer) {
        String actualPath = CharSequenceUtil.replace(path, ".", "/");
        Resource[] resources = resolver.getResources(actualPath);
        for (Resource resource : resources) {
            MetadataReader reader = readerFactory.getMetadataReader(resource);
            consumer.accept(reader);
        }
    }
}
