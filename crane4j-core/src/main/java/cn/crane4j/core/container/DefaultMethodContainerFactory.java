package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>The basic implementation of {@link MethodContainerFactory},
 * build the method data source according to the method annotated by {@link ContainerMethod}.
 *
 * @author huangchengxing
 * @see ContainerMethod
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultMethodContainerFactory implements MethodContainerFactory {

    public static final int ORDER = Integer.MAX_VALUE;
    protected final PropertyOperator propertyOperator;
    protected final AnnotationFinder annotationFinder;

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    @Override
    public int getSort() {
        return ORDER;
    }

    /**
     * Whether the method is supported.
     *
     * @param source method's calling object
     * @param method method
     * @return true if supported, false otherwise
     */
    @Override
    public boolean support(Object source, Method method) {
        return !Objects.equals(method.getReturnType(), Void.TYPE);
    }

    /**
     * Adapt methods to data source containers.
     *
     * @param source method's calling object
     * @param method method
     * @return data source containers
     */
    @Override
    public List<Container<Object>> get(Object source, Method method) {
        return annotationFinder.findAllAnnotations(method, ContainerMethod.class).stream()
            .map(annotation -> createContainer(source, method, annotation))
            .collect(Collectors.toList());
    }

    private MethodInvokerContainer createContainer(Object source, Method method, ContainerMethod annotation) {
        log.debug("create method container from [{}]", method);
        MethodInvoker methodInvoker = (t, args) -> ReflectUtil.invoke(t, method, args);
        MethodInvokerContainer.KeyExtractor keyExtractor = null;
        // if necessary, specify the method to extract the key from the returned data source object
        if (annotation.type() != MappingType.MAPPED) {
            MethodInvoker keyGetter = findKeyGetter(annotation);
            keyExtractor = keyGetter::invoke;
        }
        return new MethodInvokerContainer(
            CharSequenceUtil.emptyToDefault(annotation.namespace(), method.getName()),
            methodInvoker, source, keyExtractor, annotation.type()
        );
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
