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
public abstract class InternalProviderAssembleAnnotationHandler<T extends Annotation>
    extends AbstractStandardAssembleAnnotationHandler<T> {

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
        Class<T> annotationType, AnnotationFinder annotationFinder,
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
     * @param annotation annotation
     * @return namespace of {@link Container}
     * @implNote if the container needs to be obtained through a specific provider,
     * the name of the provider and the namespace of the container need to be concatenated through {@link ContainerManager#canonicalNamespace}
     * @see ContainerManager#canonicalNamespace
     */
    @Override
    protected String getContainerNamespace(T annotation) {
        String namespace = determineNamespace(annotation);
        // only create when the container not exist
        if (!internalContainerProvider.containsContainer(namespace)) {
            Container<Object> container = createContainer(annotation, namespace);
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
     * @param annotation annotation
     * @param namespace namespace
     * @return {@link Container} instant
     */
    @NonNull
    protected abstract Container<Object> createContainer(T annotation, String namespace);

    /**
     * Determine namespace by given annotation.
     *
     * @param annotation annotation
     * @return namespace
     */
    protected abstract String determineNamespace(T annotation);

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
