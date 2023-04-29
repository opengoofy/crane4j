package cn.crane4j.core.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;

/**
 * <p>Annotation-based {@link OperationAnnotationResolver} implementation
 * that the construction of operation configuration
 * by resolving annotations based on {@link Assemble} on classes and attributes.
 *
 * @author huangchengxing
 * @see Assemble
 * @since 1.2.0
 */
@Accessors(chain = true)
@Slf4j
public class AssembleAnnotationResolver extends StandardAssembleAnnotationResolver<Assemble> {

    /**
     * Create a {@link AssembleAnnotationResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param operationComparator operation comparator
     */
    public AssembleAnnotationResolver(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        Comparator<KeyTriggerOperation> operationComparator) {
        super(Assemble.class, annotationFinder, operationComparator, globalConfiguration);
    }

    /**
     * <p>Create a {@link AssembleAnnotationResolver} instance.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public AssembleAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Sorted.comparator());
    }

    /**
     * Get container.
     *
     * @param annotation annotation
     * @return container
     * @throws IllegalArgumentException thrown when the container is null
     */
    protected Container<?> getContainer(Assemble annotation) {
        // determine provider
        ContainerProvider provider = ConfigurationUtil.getContainerProvider(
            globalConfiguration, annotation.containerProviderName(), annotation.containerProvider()
        );
        provider = ObjectUtil.defaultIfNull(provider, globalConfiguration);
        // get from provider
        Container<?> container = CharSequenceUtil.isNotEmpty(annotation.container()) ?
            provider.getContainer(annotation.container()) : Container.empty();
        Asserts.isNotNull(container, "cannot find container [{}] from provider [{}]", annotation.container(), annotation.containerProvider());
        return container;
    }
}
