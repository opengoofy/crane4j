package cn.crane4j.springboot.support;

import cn.crane4j.annotation.Bind;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodContainerFactory;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * <p>Post process the bean, scan the method with
 * {@link ContainerMethod} annotation in the class or method of class,
 * and adapt it to {@link Container} instance
 * according to {@link MethodContainerFactory} registered in the Spring context.
 *
 * <p><b>NOTE</b>：In order to facilitate subsequent processing,
 * when looking up the method in the class according to the annotation on the class,
 * the corresponding annotation will be added to {@link Method#declaredAnnotations} through reflection.
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
     * Adapt the annotated method to {@link Container} and register it in the global configuration.
     *
     * @param bean bean
     * @param beanType bean type
     * @param annotatedMethods annotated methods
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
     * Find the corresponding method from the class
     * according to the {@link Bind} annotation in the annotation on the class.
     *
     * @param beanType bean type
     * @param classLevelAnnotation class level annotation
     * @return method corresponding to the annotation
     */
    @Nullable
    @Override
    protected Method findMethodForAnnotation(Class<?> beanType, ContainerMethod classLevelAnnotation) {
        Bind bind = classLevelAnnotation.bind();
        String methodName = CharSequenceUtil.emptyToDefault(bind.value(), classLevelAnnotation.namespace());
        Class<?>[] paramTypes = bind.paramTypes();
        Method method = ReflectionUtils.findMethod(beanType, methodName, paramTypes);
        Assert.notNull(method, "method cannot be bind to annotation: [{}]", bind);
        // bind annotation to method
        ReflectUtils.putAnnotation(classLevelAnnotation, method);
        return method;
    }

    private Collection<Container<Object>> createMethodContainer(Object bean, Method method) {
        return factories.stream()
            .filter(factory -> factory.support(bean, method))
            .findFirst()
            .map(factory -> factory.get(bean, method))
            .orElse(Collections.emptyList());
    }
}
