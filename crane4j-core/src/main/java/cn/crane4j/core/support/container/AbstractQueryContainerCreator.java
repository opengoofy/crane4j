package cn.crane4j.core.support.container;

import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * An abstract query container creator that provides access and querying to data storage based on the Repository interface.
 *
 * @param <T> The data type stored in the repository.
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractQueryContainerCreator<T> {

    @Getter
    protected final Map<String, Repository<T>> registerRepositories = new ConcurrentHashMap<>(32);
    protected final Map<CacheKey, Container<?>> containerCaches = new ConcurrentHashMap<>(32);
    protected final MethodInvokerContainerCreator methodInvokerContainerCreator;

    /**
     * Registers a repository object.
     *
     * @param name name of the repository.
     * @param target repository object.
     */
    public final void registerRepository(String name, T target) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(target);
        CollectionUtils.computeIfAbsent(registerRepositories, name, n -> createRepository(n, target));
    }

    /**
     * Creates a repository object.
     *
     * @param name name of the repository.
     * @param target repository object.
     * @return A {@link Repository} instance.
     */
    protected abstract Repository<T> createRepository(String name, T target);

    /**
     * <p>Get a container based on repository object {@link T}.<br />
     * When querying, the attribute name of the input parameter will be converted to
     * the table columns specified in the corresponding property.
     *
     * @param name mapper name
     * @param keyProperty key field name for query, if it is empty, it defaults to the specified key field
     * @param properties fields to query, if it is empty, all table columns will be queried by default.
     * @return container
     */
    public Container<?> getContainer(String name, @Nullable String keyProperty, @Nullable List<String> properties) {
        CacheKey cacheKey = new CacheKey(
            name, CharSequenceUtil.emptyToNull(keyProperty),
            CollectionUtils.defaultIfEmpty(properties, Collections.emptyList())
        );
        return CollectionUtils.computeIfAbsent(containerCaches, cacheKey, this::createContainer);
    }

    private Container<?> createContainer(CacheKey cacheKey) {
        String name = cacheKey.getMapperName();
        String keyProperty = cacheKey.getKeyProperty();
        List<String> properties = cacheKey.getProperties();
        Repository<T> repository = registerRepositories.get(name);
        Asserts.isNotNull(repository, "cannot find repository [{}]", name);

        // resolve columns for query
        Set<String> queryColumns = CollectionUtils.defaultIfEmpty(properties, Collections.emptyList())
            .stream()
            .map(p -> repository.propertyToQueryColumn(p, p))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // resolve key column for query
        keyProperty = CharSequenceUtil.emptyToDefault(keyProperty, repository.getKeyProperty());
        String keyColumn = repository.propertyToColumn(keyProperty, keyProperty);
        String keyQueryColumn = repository.propertyToQueryColumn(keyProperty, keyProperty);
        if (!queryColumns.isEmpty()) {
            queryColumns.add(keyQueryColumn);
        }

        return doCreateContainer(keyProperty, repository, queryColumns, keyColumn);
    }

    private MethodInvokerContainer doCreateContainer(
        String keyProperty, Repository<T> repository, Set<String> queryColumns, String keyColumn) {
        // generate a namespace by conditions
        String namespace = generateContainerNamespace(repository, keyColumn, queryColumns);
        MethodInvoker methodInvoker = createMethodInvoker(
            namespace, repository, queryColumns, keyColumn, keyProperty
        );
        MappingType mappingType = repository.getMappingType(keyProperty);
        return methodInvokerContainerCreator.createContainer(
            repository.getTarget(), methodInvoker, mappingType, namespace, repository.getEntityType(), keyProperty
        );
    }

    /**
     * Creates a {@link MethodInvoker} object.
     *
     * @param namespace The namespace.
     * @param repository The repository object.
     * @param queryColumns The columns to query.
     * @param keyColumn The key column.
     * @param keyProperty The key property.
     * @return A MethodInvoker object.
     */
    @Nonnull
    protected abstract MethodInvoker createMethodInvoker(
        String namespace, Repository<T> repository,
        Set<String> queryColumns, String keyColumn, String keyProperty);

    /**
     * Generates the namespace of a query container.
     *
     * @param repository The repository object.
     * @param keyColumn The key column.
     * @param queryColumns The columns to query.
     * @return The namespace of the query container.
     */
    @Nonnull
    protected String generateContainerNamespace(
        Repository<T> repository, String keyColumn, Collection<String> queryColumns) {
        return CharSequenceUtil.format(
            "select {} from {} where {} in ?",
            queryColumns.isEmpty() ? "*" : String.join(", ", queryColumns),
            repository.getTableName(),
            keyColumn
        );
    }

    /**
     * Clear all caches
     */
    public void destroy() {
        this.registerRepositories.clear();
        this.containerCaches.clear();
    }

    /**
     * An interface for a generic repository that provides access to data storage.
     *
     * @param <T> the type of data stored in the repository.
     */
    public interface Repository<T> {

        /**
         * Returns the target object stored in the repository.
         *
         * @return the target object.
         */
        T getTarget();

        /**
         * Returns the name of the table where the data is stored.
         *
         * @return the table name.
         */
        String getTableName();

        /**
         * Returns the mapping type for a given property.
         *
         * @param property the property to map.
         * @return the mapping type.
         */
        default MappingType getMappingType(String property) {
            return MappingType.ONE_TO_ONE;
        }

        /**
         * Returns the entity type of the stored data.
         *
         * @return the entity type.
         */
        Class<?> getEntityType();

        /**
         * Returns the key property for the data stored in the repository.
         *
         * @return the key property.
         */
        String getKeyProperty();

        /**
         * Converts a property name to its corresponding column name.<br />
         * eg:
         * <pre>
         *     userName -> user_name
         *     userAge -> user_age
         *     id -> id as id
         * </pre>
         *
         * @param property the property to convert.
         * @param defaultValue the default value to return if no matching column is found.
         * @return the column name.
         */
        String propertyToColumn(String property, String defaultValue);

        /**
         * Converts a property name to its corresponding query column name.<br />
         * eg:
         * <pre>
         *     userName -> user_name as userName
         *     userAge -> user_age as userAge
         *     id -> id as id
         * </pre>
         *
         * @param property the property to convert.
         * @param defaultValue the default value to return if no matching query column is found.
         * @return the query column name.
         */
        String propertyToQueryColumn(String property, String defaultValue);
    }


    /**
     * The cache key.
     */
    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    protected static class CacheKey {
        private final String mapperName;
        @Nullable
        private final String keyProperty;
        @Nullable
        private final List<String> properties;
    }
}
