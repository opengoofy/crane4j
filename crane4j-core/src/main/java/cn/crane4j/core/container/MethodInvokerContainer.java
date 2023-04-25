package cn.crane4j.core.container;

import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.lang.Assert;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>Method data source container, specify any method without parameters
 * or the first parameter is {@link Collection} type method,
 * and adapt it to a data source container.
 *
 * <p>This container is generally not created directly by users,
 * but is used in conjunction with {@link MethodContainerFactory} to scan the methods
 * in a large number of specific context objects in the framework
 * according to annotations or specific configurations,
 * automatically adapt them to data source containers and register them.
 *
 * <p>If only a few known methods need to be used as data sources,
 * you can directly use {@link LambdaContainer}.
 *
 * @author huangchengxing
 * @see MethodContainerFactory
 * @see MethodInvokerContainerCreator
 * @see MappingType
 */
public class MethodInvokerContainer implements Container<Object> {

    @Getter
    private final String namespace;
    private final MethodInvoker methodInvoker;
    private final Object methodSource;
    private final KeyExtractor keyExtractor;
    private final MappingType mappingType;

    /**
     * Build a method data source container.
     *
     * @param namespace namespace
     * @param methodInvoker method to call
     * @param methodSource object to be called
     * @param keyExtractor key value extraction method of the data source object
     * @param mappingType mapping relationship between the object returned by the method and the target object
     */
    public MethodInvokerContainer(
        String namespace,
        MethodInvoker methodInvoker, Object methodSource,
        KeyExtractor keyExtractor, MappingType mappingType) {
        this.namespace = Objects.requireNonNull(namespace);
        this.methodInvoker = Objects.requireNonNull(methodInvoker);
        this.methodSource = methodSource;

        // if the return value is not Map, the key extractor is required
        this.keyExtractor = keyExtractor;
        this.mappingType = Objects.requireNonNull(mappingType);
        Assert.isTrue(
            mappingType == MappingType.MAPPED || Objects.nonNull(keyExtractor),
            "keyExtractor must not null"
        );
    }

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, ?> get(Collection<Object> keys) {
        Object invokeResult = methodInvoker.invoke(methodSource, keys);
        if (Objects.isNull(invokeResult)) {
            return Collections.emptyMap();
        }
        if (mappingType == MappingType.MAPPED) {
            return (Map<Object, ?>)invokeResult;
        }
        // group return values by type
        Collection<?> invokeResults = CollectionUtils.adaptObjectToCollection(invokeResult);
        // one to one
        if (mappingType == MappingType.ONE_TO_ONE) {
            return invokeResults.stream().collect(Collectors.toMap(keyExtractor::getKey, Function.identity()));
        }
        // one to many
        return invokeResults.stream().collect(Collectors.groupingBy(keyExtractor::getKey));
    }

    /**
     * The key value extractor is used to obtain the key value from the data source object.
     */
    @FunctionalInterface
    public interface KeyExtractor {

        /**
         * Get key value from source object.
         *
         * @param source source object
         * @return key value
         */
        Object getKey(Object source);
    }
}
