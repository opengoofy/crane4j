package cn.createsequence.crane4j.springboot.support;

import cn.createsequence.crane4j.core.annotation.Bind;
import cn.createsequence.crane4j.core.annotation.ContainerMethod;
import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.container.MethodContainerFactory;
import cn.createsequence.crane4j.core.container.MethodInvokerContainer;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>对Bean进行后置处理，扫描bean中带有{@link ContainerMethod}注解的方法，
 * 以及类上{@link ContainerMethod}所指向的方法，
 * 根据注册到Spring容器中的{@link MethodContainerFactory}将其适配为{@link Container}实例。
 *
 * <p>注意：为了便于后续处理，根据类上注解查找类中方法时，
 * 将会把对应的注解通过反射加入{@link Method#declaredAnnotations}里。
 *
 * @author huangchengxing
 * @see ContainerMethod
 * @see MethodContainerFactory
 * @see Crane4jApplicationContext
 */
@Order
@Slf4j
public class AnnotationMethodContainerProcessor extends AbstractAnnotatedMethodPostProcessor<ContainerMethod> {

    protected final Collection<MethodContainerFactory> factories;
    protected final Crane4jApplicationContext configuration;

    public AnnotationMethodContainerProcessor(
        Collection<MethodContainerFactory> factories, Crane4jApplicationContext configuration) {
        super(ContainerMethod.class);
        this.factories = factories.stream()
            .sorted(Comparator.comparing(MethodContainerFactory::getSort))
            .collect(Collectors.toList());
        this.configuration = configuration;
    }

    /**
     * 将被注解的方法适配为{@link MethodInvokerContainer}，并注册的全局配置类中
     *
     * @param bean 目标对象
     * @param beanType 目标类型
     * @param annotatedMethods 被注解的方法
     */
    @Override
    protected void processAnnotatedMethods(
        Object bean, Class<?> beanType, Multimap<Method, ContainerMethod> annotatedMethods) {
        Collection<Container<Object>> containers = annotatedMethods.keys().stream()
            .map(method -> createMethodContainer(bean, method))
            .filter(CollUtil::isNotEmpty)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        containers.forEach(configuration::registerContainer);
    }

    /**
     * 根据类上注解中的{@link Bind}注解，从类中寻找对应的方法
     *
     * @param beanType 目标类型
     * @param classLevelAnnotation 类上的注解
     * @return 与注解对应的方法，若不存在则为{@code null}
     */
    @Nullable
    @Override
    protected Method findMethodForAnnotation(Class<?> beanType, ContainerMethod classLevelAnnotation) {
        Bind bind = classLevelAnnotation.bind();
        String methodName = CharSequenceUtil.emptyToDefault(bind.value(), classLevelAnnotation.namespace());
        Class<?>[] paramTypes = bind.paramTypes();
        Method method = ReflectionUtils.findMethod(beanType, methodName, paramTypes);
        Assert.notNull(method, "method cannot be bind to annotation: [{}]", bind);
        // 将注解绑定到方法上
        bindAnnotationsToMethod(classLevelAnnotation, method);
        return method;
    }

    @SuppressWarnings("unchecked")
    private static void bindAnnotationsToMethod(ContainerMethod classLevelAnnotation, Method method) {
        if (Objects.nonNull(method)) {
            // 调用方法触发初始化
            Objects.requireNonNull(method.getAnnotations());
            Optional.ofNullable(ReflectUtil.getFieldValue(method, "declaredAnnotations"))
                .map(map -> (Map<Class<? extends Annotation >, Annotation>)map)
                .map(LinkedHashMap::new)
                .ifPresent(map -> {
                    map.put(ContainerMethod.class, classLevelAnnotation);
                    ReflectUtil.setFieldValue(method, "declaredAnnotations", Collections.unmodifiableMap(map));
                });
        }
    }

    private Collection<Container<Object>> createMethodContainer(Object bean, Method method) {
        return factories.stream()
            .filter(factory -> factory.support(bean, method))
            .findFirst()
            .map(factory -> factory.get(bean, method))
            .orElse(Collections.emptyList());
    }
}
