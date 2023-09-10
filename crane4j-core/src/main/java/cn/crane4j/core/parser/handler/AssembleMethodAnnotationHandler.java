package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleMethod;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.container.ContainerMethodSupport;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * <p>An {@link AbstractAssembleAnnotationHandler} implementation for {@link AssembleMethod} annotation.
 *
 * <p>When processing {@link AssembleMethod} annotation,
 * it will create a {@link Container} for method which specified by {@link ContainerMethod},
 * and then register the {@link Container} to {@link #internalContainerProvider}.<br />
 * The target of method will be created by {@link #getTargetInstance}, by default it will create a new instance of target class.
 *
 * @author huangchengxing
 * @see AssembleMethod
 * @see ContainerMethodSupport
 * @since 2.2.0
 */
@Slf4j
public class AssembleMethodAnnotationHandler
    extends InternalProviderAssembleAnnotationHandler<AssembleMethod> {

    protected final ContainerMethodResolver containerMethodResolver;

    /**
     * Create an {@link AssembleMethodAnnotationHandler} instance.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration global configuration
     * @param methodContainerFactories method container factories
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleMethodAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        Collection<MethodContainerFactory> methodContainerFactories,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(AssembleMethod.class, annotationFinder, Crane4jGlobalSorter.comparator(), globalConfiguration, propertyMappingStrategyManager);
        this.containerMethodResolver = new ContainerMethodResolver(methodContainerFactories);
    }

    /**
     * Create container by given annotation and namespace.
     *
     * @param annotation annotation
     * @param namespace  namespace
     * @return {@link Container} instant
     */
    @Override
    protected @NonNull Container<Object> createContainer(AssembleMethod annotation, String namespace) {
        Container<Object> container = containerMethodResolver.resolve(annotation);
        Asserts.isNotNull(container, "cannot resolve container for annotation {}", annotation);
        return new AssembleMethodContainer<>(namespace, container);
    }

    /**
     * Determine namespace by given annotation.
     *
     * @param annotation annotation
     * @return namespace
     */
    @Override
    protected String determineNamespace(AssembleMethod annotation) {
        return StringUtils.md5DigestAsHex(StringUtils.join(
            String::valueOf, "#", annotation.method(), annotation.target(), annotation.targetType()
        ));
    }

    /**
     * Get {@link StandardAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element        element
     * @param annotation     annotation
     * @return {@link StandardAnnotation} instance
     */
    @Override
    protected StandardAnnotation getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleMethod annotation) {
        return new StandardAnnotationAdapter(
            annotation, annotation.key(), annotation.sort(),
            annotation.handler(), annotation.handlerType(),
            annotation.propTemplates(), annotation.props(), annotation.groups(),
            annotation.propertyMappingStrategy()
        );
    }

    /**
     * Resolve target type.
     *
     * @param annotation annotation
     * @return target type
     */
    @NonNull
    protected Class<?> resolveTargetType(AssembleMethod annotation) {
        return ClassUtils.forName(annotation.target(), annotation.targetType());
    }
    
    /**
     * Get target by given type and annotation.
     *
     * @param targetType target type
     * @param annotation annotation
     * @return target instance
     */
    @Nullable
    protected Object getTargetInstance(Class<?> targetType, AssembleMethod annotation) {
        try {
            return ClassUtils.newInstance(targetType);
        } catch(Exception ex) {
            log.warn("cannot create target instance of assemble method for class [{}]", targetType);
            throw ex;
        }
    }

    /**
     * A resolve for {@link AssembleMethod}.
     *
     * @author huangchengxing
     */
    protected class ContainerMethodResolver extends ContainerMethodSupport {

        /**
         * Create a {@link ContainerMethodSupport} instance.
         *
         * @param methodContainerFactories method container factories
         */
        protected ContainerMethodResolver(Collection<MethodContainerFactory> methodContainerFactories) {
            super(methodContainerFactories);
        }

        /**
         * Get container from given {@code type} and {@code annotation}.
         *
         * @param annotation annotation
         * @return container instance if found, null otherwise
         */
        @Nullable
        public Container<Object> resolve(AssembleMethod annotation) {
            Class<?> targetType = resolveTargetType(annotation);
            Asserts.isNotNull(targetType, "cannot resolve target type for annotation {}", annotation);
            Method resolvedMethod = getContainerMethod(annotation, targetType);
            Asserts.isNotNull(
                resolvedMethod, "cannot resolve method of class [{}] for annotation [{}]", targetType, annotation
            );
            Object target = Modifier.isStatic(resolvedMethod.getModifiers()) ?
                null : getTargetInstance(targetType, annotation);
            return CollectionUtils.get(
                createMethodContainer(target, resolvedMethod, Collections.singleton(annotation.method())), 0
            );
        }

        @Nullable
        private Method getContainerMethod(AssembleMethod annotation, Class<?> targetType) {
            Method[] methods = ReflectUtils.getMethods(targetType);
            return findMatchedMethodForAnnotation(Arrays.asList(methods), annotation.method());
        }
    }

    /**
     * An internal container, used to cover namespace of container which create by resolver.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    private static class AssembleMethodContainer<T> implements Container<T> {
        @Getter
        private final String namespace;
        private final Container<T> container;
        @Override
        public Map<T, ?> get(Collection<T> keys) {
            return container.get(keys);
        }
    }
}
