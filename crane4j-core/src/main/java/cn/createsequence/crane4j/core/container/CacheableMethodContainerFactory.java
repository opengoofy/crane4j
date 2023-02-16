package cn.createsequence.crane4j.core.container;

import cn.createsequence.crane4j.core.annotation.ContainerCache;
import cn.createsequence.crane4j.core.cache.Cache;
import cn.createsequence.crane4j.core.cache.CacheManager;
import cn.createsequence.crane4j.core.support.AnnotationFinder;
import cn.createsequence.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link DefaultMethodContainerFactory}的扩展实现，
 * 在前者的基础上，若方法上存在{@link ContainerCache}注解，
 * 则将得到的方法容器包装为{@link CacheableContainer}。
 *
 * @author huangchengxing
 * @see ContainerCache
 */
public class CacheableMethodContainerFactory extends DefaultMethodContainerFactory {

    public static final int ORDER = DefaultMethodContainerFactory.ORDER - 1;
    private final CacheManager cacheManager;

    public CacheableMethodContainerFactory(
        PropertyOperator propertyOperator, AnnotationFinder annotationFinder, CacheManager cacheManager) {
        super(propertyOperator, annotationFinder);
        this.cacheManager = cacheManager;
    }

    /**
     * 获取排序值，越小越优先执行
     *
     * @return 排序值
     */
    @Override
    public int getSort() {
        return ORDER;
    }

    /**
     * 是否支持处理该方法
     *
     * @param source 方法的调用对象
     * @param method 方法
     * @return 是否
     */
    @Override
    public boolean support(Object source, Method method) {
        ContainerCache annotation = annotationFinder.findAnnotation(method, ContainerCache.class);
        return Objects.nonNull(annotation) && super.support(source, method);
    }

    /**
     * 获取方法数据源
     *
     * @param source 方法的调用对象
     * @param method 方法
     * @return 方法数据源容器
     */
    @Override
    public List<Container<Object>> get(Object source, Method method) {
        ContainerCache annotation = annotationFinder.findAnnotation(method, ContainerCache.class);
        // 若未指定cacheName，则默认取容器的namespace
        Function<Container<Object>, Cache<Object>> containerFactory = CharSequenceUtil.isEmpty(annotation.value()) ?
            container -> cacheManager.getCache(annotation.value()) : container -> cacheManager.getCache(container.getNamespace());
        return super.get(source, method).stream()
            .map(container -> new CacheableContainer<>(container, containerFactory.apply(container)))
            .collect(Collectors.toList());
    }
}
