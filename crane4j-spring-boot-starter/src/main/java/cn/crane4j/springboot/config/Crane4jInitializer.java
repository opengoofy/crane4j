package cn.crane4j.springboot.config;

import cn.crane4j.core.annotation.ContainerConstant;
import cn.crane4j.core.annotation.ContainerEnum;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 默认的初始化器，用于应用启动后，针对一些缓存或组件进行初始化处理
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
        // 加载枚举并将其注册为容器
        loadContainerEnum();
        // 加载常量类并将其注册为容器
        loadConstantClass();
        // 预解析类操作配置
        loadOperateEntity();
        // 包装需要换成的数据源容器
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
        Map<String, Container<?>> containerMap = configuration.getRegisteredContainers();
        crane4jProperties.getCacheContainers().forEach((cacheName, namespaces) -> {
            Cache<Object> cache = cacheManager.getCache(cacheName);
            for (String namespace : namespaces) {
                containerMap.computeIfPresent(
                    namespace, (n, container) -> new CacheableContainer<>((Container<Object>)container, cache)
                );
            }
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
