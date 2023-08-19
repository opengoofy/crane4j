package cn.crane4j.extension.spring.util;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ObjectUtils;
import cn.crane4j.core.util.StringUtils;
import cn.crane4j.extension.spring.annotation.ConstantContainerScan;
import cn.crane4j.extension.spring.annotation.EnumContainerScan;
import cn.crane4j.extension.spring.annotation.OperatorScan;
import cn.crane4j.extension.spring.scanner.ClassScanner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.StringValueResolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A processor that processes the {@link ConstantContainerScan} and {@link EnumContainerScan} annotations.
 *
 * @author huangchengxing
 * @see OperatorScan
 * @see ConstantContainerScan
 * @see EnumContainerScan
 * @since 2.1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ContainerScanUtils {

    /**
     * Load container from the specified class type which is annotated by {@link ContainerConstant}.
     *
     * @param types the specified class type
     * @param containerManager container manager
     * @param annotationFinder annotation finder
     * @return container instances is loaded
     */
    public static Collection<Container<Object>> loadConstantClass(
        Set<Class<?>> types, ContainerManager containerManager, AnnotationFinder annotationFinder) {
        return resolveConstantContainers(types, annotationFinder).stream()
            .filter(container -> !containerManager.containsContainer(container.getNamespace()))
            .peek(containerManager::registerContainer)
            .collect(Collectors.toList());
    }

    /**
     * Resolve container from the specified class type which is annotated by {@link ContainerConstant}.
     *
     * @param types the specified class type
     * @param annotationFinder annotation finder
     * @return container instances
     */
    public static Collection<Container<Object>> resolveConstantContainers(
        Set<Class<?>> types, AnnotationFinder annotationFinder) {
        return types.stream()
            .filter(type -> AnnotatedElementUtils.isAnnotated(type, ContainerConstant.class))
            .map(type -> Containers.forConstantClass(type, annotationFinder))
            .collect(Collectors.toList());
    }

    /**
     * Load container from the specified class type which is annotated by {@link ContainerEnum}.
     *
     * @param types the specified class type
     * @param isOnlyLoadAnnotatedEnum whether only load the class type which is annotated by {@link ContainerEnum}
     * @param containerManager container manager
     * @param annotationFinder annotation finder
     * @param propertyOperator property operator
     * @return container instances which is loaded
     */
    public static Collection<Container<Object>> loadContainerEnum(
        Set<Class<?>> types, boolean isOnlyLoadAnnotatedEnum,
        ContainerManager containerManager, AnnotationFinder annotationFinder, PropertyOperator propertyOperator) {
        return resolveEnumContainers(types, isOnlyLoadAnnotatedEnum, annotationFinder, propertyOperator).stream()
            .filter(container -> !containerManager.containsContainer(container.getNamespace()))
            .peek(containerManager::registerContainer)
            .collect(Collectors.toList());
    }

    /**
     * Resolve container from the specified class type which is annotated by {@link ContainerEnum}.
     *
     * @param types the specified class type
     * @param isOnlyLoadAnnotatedEnum whether only load the class type which is annotated by {@link ContainerEnum}
     * @param annotationFinder annotation finder
     * @param propertyOperator property operator
     * @return container instances
     */
    @SuppressWarnings("unchecked")
    public static Collection<Container<Object>> resolveEnumContainers(
        Set<Class<?>> types, boolean isOnlyLoadAnnotatedEnum,
        AnnotationFinder annotationFinder, PropertyOperator propertyOperator) {
        return types.stream()
            .filter(Class::isEnum)
            .filter(type -> !isOnlyLoadAnnotatedEnum || AnnotatedElementUtils.isAnnotated(type, ContainerEnum.class))
            .map(type -> Containers.forEnum((Class<Enum<?>>) type, annotationFinder, propertyOperator))
            .collect(Collectors.toList());
    }

    /**
     * Resolve component types from annotation metadata.
     *
     * @param annotationAttributes annotation attributes
     * @param classScanner class scanner
     * @param stringValueResolver string value resolver
     * @return component types
     */
    public static Set<Class<?>> resolveComponentTypesFromMetadata(
        AnnotationAttributes annotationAttributes, ClassScanner classScanner, @Nullable StringValueResolver stringValueResolver) {
        if (Objects.isNull(annotationAttributes)) {
            return Collections.emptySet();
        }
        Set<String> includePackages = getIncludePackages(annotationAttributes, ObjectUtils.defaultIfNull(stringValueResolver, str -> str));
        Set<Class<?>> includeTypes = includePackages.stream()
            .map(classScanner::scan)
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        includeTypes.addAll(getIncludeClasses(annotationAttributes));
        includeTypes.removeAll(getExcludeClasses(annotationAttributes));
        return includeTypes;
    }

    /**
     * Get package path which will be scanned.
     *
     * @param annotationAttributes annotation attributes
     * @return package path
     */
    private static Set<String> getIncludePackages(AnnotationAttributes annotationAttributes, StringValueResolver stringValueResolver) {
        return Stream.of(annotationAttributes.getStringArray("includePackages"))
            .filter(StringUtils::isNotEmpty)
            .map(stringValueResolver::resolveStringValue)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toSet());
    }

    /**
     * Get class type which will be scanned.
     *
     * @param annotationAttributes annotation attributes
     * @return class type
     */
    private static Set<Class<?>> getIncludeClasses(AnnotationAttributes annotationAttributes) {
        Class<?>[] classes = annotationAttributes.getClassArray("includeClasses");
        return Arrays.stream(classes).collect(Collectors.toSet());
    }

    /**
     * Get class type which will not be scanned.
     *
     * @param annotationAttributes annotation attributes
     * @return class type
     */
    private static Set<Class<?>> getExcludeClasses(AnnotationAttributes annotationAttributes) {
        Class<?>[] classes = annotationAttributes.getClassArray("excludeClasses");
        return Arrays.stream(classes).collect(Collectors.toSet());
    }
}
