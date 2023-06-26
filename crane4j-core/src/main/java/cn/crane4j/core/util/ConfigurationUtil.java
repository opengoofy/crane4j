package cn.crane4j.core.util;

import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.support.AnnotationFinder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ConfigurationUtil
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationUtil {

    // ================ trigger lifecycle callback ================

    /**
     * trigger {@link ContainerLifecycleProcessor#whenDestroyed}
     *
     * @param target container comparator or container definition
     */
    public static void triggerWhenDestroyed(
        Object target, Collection<ContainerLifecycleProcessor> containerLifecycleProcessorList) {
        containerLifecycleProcessorList.forEach(processor -> processor.whenDestroyed(target));
    }

    /**
     * trigger {@link ContainerLifecycleProcessor#whenRegistered}
     *
     * @param definition definition
     * @param namespace namespace
     * @param old old container comparator or container definition
     * @return container definition
     */
    @Nullable
    public static ContainerDefinition triggerWhenRegistered(
        ContainerDefinition definition, String namespace, Object old,
        Collection<ContainerLifecycleProcessor> containerLifecycleProcessorList, Logger log) {
        for (ContainerLifecycleProcessor containerLifecycleProcessor : containerLifecycleProcessorList) {
            definition = containerLifecycleProcessor.whenRegistered(old, definition);
            if (Objects.isNull(definition)) {
                log.info("not register container definition for [{}]", namespace);
                return null;
            }
        }
        return definition;
    }

    /**
     * trigger {@link ContainerLifecycleProcessor#whenCreated}
     *
     * @param namespace namespace
     * @param container container
     * @param definition definition
     * @return container comparator
     */
    @Nullable
    public static Container<Object> triggerWhenCreated(
        String namespace, ContainerDefinition definition, Container<Object> container,
        Collection<ContainerLifecycleProcessor> containerLifecycleProcessorList, Logger log) {
        for (ContainerLifecycleProcessor containerLifecycleProcessor : containerLifecycleProcessorList) {
            container = containerLifecycleProcessor.whenCreated(definition, container);
            if (Objects.isNull(container)) {
                log.warn(
                        "not create container for [{}], because of container lifecycle processor [{}]",
                        namespace, containerLifecycleProcessor.getClass().getName()
                );
                break;
            }
        }
        return container;
    }

    // ==================== parsing ====================

    public static List<PropertyMapping> parsePropTemplateClasses(Class<?>[] annotatedTypes, AnnotationFinder annotationFinder) {
        return Stream.of(annotatedTypes)
            .map(type -> annotationFinder.findAnnotation(type, MappingTemplate.class))
            .map(ConfigurationUtil::parsePropTemplate)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    public static List<PropertyMapping> parsePropTemplate(MappingTemplate annotation) {
        return Stream.of(annotation.value())
            .map(ConfigurationUtil::createPropertyMapping)
            .collect(Collectors.toList());
    }

    public static PropertyMapping createPropertyMapping(Mapping annotation) {
        return createPropertyMapping(annotation, "");
    }

    public static PropertyMapping createPropertyMapping(Mapping annotation, String defaultReference) {
        if (StringUtils.isNotEmpty(annotation.value())) {
            return new SimplePropertyMapping(annotation.value(), annotation.value());
        }
        String ref = StringUtils.emptyToDefault(annotation.ref(), defaultReference);
        return new SimplePropertyMapping(annotation.src(), ref);
    }
}
