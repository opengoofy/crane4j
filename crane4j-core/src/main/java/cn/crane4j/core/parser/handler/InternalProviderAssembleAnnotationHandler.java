package cn.crane4j.core.parser.handler;

import cn.crane4j.core.container.ConfigurableContainerProvider;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.PartitionContainerProvider;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.util.Asserts;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;
import java.util.Comparator;

/**
 * <p>一个基于{@link AbstractStandardAssembleAnnotationHandler}的扩展实现，
 * 用于支持在处理操作注解时，一并为其创建并注册对应的容器。
 *
 * <p>该注解处理器在内部维护了一个{@link ContainerProvider}实例，
 * 当从操作注解中解析出容器的命名空间时，会尝试根据规则创建一个容器实例，并存储在内部的提供者中。
 *
 * <p>该内部提供者在处理器实例创建时会注册到全局容器管理器中。
 * 通常情况下，该提供者可以在{@link ContainerManager}根据名称{@code {处理器的简单类名}.InternalProvider}进行访问，
 * 但<strong>不建议在处理器外部操作它</strong>。
 *
 * <hr/>
 *
 * <p>An implementation of {@link AbstractStandardAssembleAnnotationHandler} that
 * creates corresponding containers while processing operation annotations.
 *
 * <p>The annotation handler maintains a {@link ContainerProvider} instance internally,
 * and when the container's namespace is resolved from the operation annotation,
 * it will try to create a container instance for it
 * according to the rules and store it in the internal provider.
 *
 * <p>The internal provider registers with
 * the global container manager when the handler instance is created.
 * the provider is usually accessible by the name {@code {simple class name of handler}.InternalProvider},
 * but it is <strong>not recommended to operate on it outside the handler</strong>.
 *
 * @author huangchengxing
 * @see ConfigurableContainerProvider
 * @since 2.2.0
 */
public abstract class InternalProviderAssembleAnnotationHandler<A extends Annotation>
    extends AbstractStandardAssembleAnnotationHandler<A> {

    public static final String INTERNAL_PROVIDER_SUFFIX = ".InternalProvider";

    /**
     * Internal container provider.
     */
    protected final ConfigurableContainerProvider internalContainerProvider;

    /**
     * Create an {@link AbstractStandardAssembleAnnotationHandler} instance.
     *
     * @param annotationType                 annotation type
     * @param annotationFinder               annotation finder
     * @param operationComparator            operation comparator
     * @param globalConfiguration            global configuration
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    protected InternalProviderAssembleAnnotationHandler(
        Class<A> annotationType, AnnotationFinder annotationFinder,
        @NonNull Comparator<KeyTriggerOperation> operationComparator,
        Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(annotationType, annotationFinder, operationComparator, globalConfiguration, propertyMappingStrategyManager);

        // init internal container provider
        internalContainerProvider = createInternalContainerProvider();
        globalConfiguration.registerContainerProvider(
            getInternalContainerProviderName(), internalContainerProvider
        );
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param standardAnnotation standard annotation
     * @return namespace of {@link Container}
     * @implNote if the container needs to be obtained through a specific provider,
     * the name of the provider and the namespace of the container need to be concatenated through {@link ContainerManager#canonicalNamespace}
     * @see ContainerManager#canonicalNamespace
     */
    @Override
    protected String getContainerNamespace(StandardAssembleAnnotation<A> standardAnnotation) {
        A annotation = standardAnnotation.getAnnotation();
        String namespace = determineNamespace(standardAnnotation);
        // only create when the container not exist
        if (!internalContainerProvider.containsContainer(namespace)) {
            Container<Object> container = createContainer(standardAnnotation, namespace);
            Asserts.isNotNull(
                container, "cannot resolve container for annotation [{}]", annotation
            );
            internalContainerProvider.registerContainer(container);
        }
        return ContainerManager.canonicalNamespace(
            namespace, getInternalContainerProviderName()
        );
    }

    /**
     * Create container by given annotation and namespace.
     *
     * @param standardAnnotation standard annotation
     * @param namespace namespace
     * @return {@link Container} instant
     */
    @NonNull
    protected abstract Container<Object> createContainer(
        StandardAssembleAnnotation<A> standardAnnotation, String namespace);

    /**
     * Determine namespace by given annotation.
     *
     * @param standardAnnotation standard annotation
     * @return namespace
     */
    protected abstract String determineNamespace(StandardAssembleAnnotation<A> standardAnnotation);

    /**
     * Create internal container provider.
     *
     * @return {@link ContainerProvider} instant
     */
    @NonNull
    protected ConfigurableContainerProvider createInternalContainerProvider() {
        return new PartitionContainerProvider();
    }

    /**
     * Get the name of internal container provider.
     *
     * @return provider name
     */
    public String getInternalContainerProviderName() {
        return getClass().getSimpleName() + INTERNAL_PROVIDER_SUFFIX;
    }
}
