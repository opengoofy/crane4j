package cn.createsequence.crane4j.core.container;

import cn.createsequence.crane4j.core.annotation.ContainerMethod;
import cn.createsequence.crane4j.core.annotation.MappingType;
import cn.createsequence.crane4j.core.support.AnnotationFinder;
import cn.createsequence.crane4j.core.support.MethodInvoker;
import cn.createsequence.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link MethodContainerFactory}的基本实现，
 * 用于根据被{@link ContainerMethod}注解的方法构建方法数据源
 *
 * @author huangchengxing
 * @see ContainerMethod
 */
@RequiredArgsConstructor
public class DefaultMethodContainerFactory implements MethodContainerFactory {

    private final PropertyOperator propertyOperator;
    private final AnnotationFinder annotationFinder;

    /**
     * 是否支持处理该方法
     *
     * @param source 方法的调用对象
     * @param method 方法
     * @return 是否
     */
    @Override
    public boolean support(Object source, Method method) {
        return !Objects.equals(method.getReturnType(), Void.TYPE);
    }

    /**
     * 获取方法数据源
     *
     * @param source 方法的调用对象
     * @param method 方法
     * @return 方法数据源容器
     */
    @Override
    public List<MethodInvokerContainer> get(Object source, Method method) {
        return annotationFinder.findAllAnnotations(method, ContainerMethod.class).stream()
            .map(annotation -> createContainer(source, method, annotation))
            .collect(Collectors.toList());
    }

    private MethodInvokerContainer createContainer(Object source, Method method, ContainerMethod annotation) {
        MethodInvoker methodInvoker = (t, args) -> ReflectUtil.invoke(t, method, args);
        MethodInvokerContainer.KeyExtractor keyExtractor = null;
        // 若有必要，指定从返回的数据源对象提取key的男方法
        if (annotation.type() != MappingType.MAPPED) {
            MethodInvoker keyGetter = findKeyGetter(annotation);
            keyExtractor = keyGetter::invoke;
        }
        return new MethodInvokerContainer(annotation.namespace(), methodInvoker, source, keyExtractor, annotation.type());
    }

    private MethodInvoker findKeyGetter(ContainerMethod annotation) {
        Class<?> resultType = annotation.resultType();
        String resultKey = annotation.resultKey();
        MethodInvoker keyGetter =  propertyOperator.findGetter(resultType, resultKey);
        Objects.requireNonNull(keyGetter, CharSequenceUtil.format(
            "cannot find getter method [{}] on [{}]", resultKey, resultType
        ));
        return keyGetter;
    }
}
