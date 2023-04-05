package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.injector.AbstractSqlInjector;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>A processor and register of MybatisPlus {@link BaseMapper}.
 * After the Spring context is initialized, scan the {@link BaseMapper} in the context,
 * And adapt its {@link BaseMapper#selectList} method to
 * the container according to the configuration.
 *
 * @author huangchengxing
 * @see MpMethodContainer
 */
@Slf4j
@RequiredArgsConstructor
public class MpBaseMapperContainerRegister {

    protected final Crane4jGlobalConfiguration crane4jGlobalConfiguration;
    @Getter
    protected final Map<String, MapperInfo> registerMappers = new HashMap<>(32);
    protected final PropertyOperator propertyOperator;
    private final Map<CacheKey, Container<?>> containerCaches = new ConcurrentHashMap<>(32);

    // ================== public ==================

    /**
     * Register mapper.
     *
     * @param name mapper name, or table name
     * @param baseMapper baseMapper
     */
    public final void registerMapper(String name, BaseMapper<?> baseMapper) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(baseMapper);
        registerMappers.computeIfAbsent(name, n -> {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(n);
            if (Objects.isNull(tableInfo)) {
                tableInfo = Optional.ofNullable(Proxy.getInvocationHandler(baseMapper))
                    .map(h -> (Class<?>)ReflectUtil.getFieldValue(h, "mapperInterface"))
                    .map(this::extractModelClass)
                    .map(TableInfoHelper::getTableInfo)
                    .orElseThrow(() -> new Crane4jException("cannot resolve bean type of mapper [{}]", name));
            }
            return new MapperInfo(n, baseMapper, tableInfo);
        });
    }

    /**
     * <p>Get a container based on {@link BaseMapper#selectList}.<br />
     * When querying, the attribute name of the input parameter will be converted to
     * the table columns specified in the corresponding {@link TableField} annotation.
     *
     * @param name mapper name
     * @param keyProperty key field name for query, if it is empty, it defaults to the field annotated by {@link TableId}
     * @param properties fields to query, if it is empty, all table columns will be queried by default.
     * @return container
     * @see TableField
     * @see TableId
     */
    public Container<?> getContainer(String name, @Nullable String keyProperty, @Nullable List<String> properties) {
        CacheKey cacheKey = new CacheKey(
            name,
            CharSequenceUtil.emptyToNull(keyProperty),
            CollUtil.defaultIfEmpty(properties, Collections.emptyList())
        );
        return MapUtil.computeIfAbsent(containerCaches, cacheKey, this::doGetContainer);
    }

    /**
     * Invoked by the containing {@code BeanFactory} on destruction of a bean.
     *
     */
    public void destroy() {
        registerMappers.clear();
        containerCaches.clear();
    }

    /**
     * Generate namespace of container.
     *
     * @param mapperInfo mapper info
     * @param keyColumn key property
     * @param queryColumns query columns
     * @return namespace of container
     */
    protected String generateContainerNamespace(
        MapperInfo mapperInfo, String keyColumn, String[] queryColumns) {
        return CharSequenceUtil.format(
            "select {} from {} where {} in ?",
            ArrayUtil.isEmpty(queryColumns) ? "*" : ArrayUtil.join(queryColumns, ", "),
            mapperInfo.getTableInfo().getTableName(),
            keyColumn
        );
    }

    /**
     * Create a {@link Container} instance.
     *
     * @param namespace namespace
     * @param mapper mapper
     * @param queryColumns query columns
     * @param keyColumn key column
     * @param keyGetter key getter
     * @return {@link Container}
     */
    protected Container<?> doCreateContainer(
        String namespace, BaseMapper<?> mapper, String[] queryColumns, String keyColumn, MethodInvoker keyGetter) {
        return new MpMethodContainer<>(namespace, mapper, queryColumns, keyColumn, keyGetter);
    }

    // ================== private ==================

    private Container<?> doGetContainer(CacheKey cacheKey) {
        String mapperName = cacheKey.getMapperName();
        String keyProperty = cacheKey.getKeyProperty();
        List<String> properties = cacheKey.getProperties();

        MapperInfo info = registerMappers.get(mapperName);
        Assert.notNull(info, "cannot find mapper [{}]", mapperName);
        TableInfo tableInfo = info.getTableInfo();

        // resolve query columns
        Map<String, TableFieldInfo> propertyMap = tableInfo.getFieldList().stream()
            .collect(Collectors.toMap(f -> f.getField().getName(), Function.identity()));
        String[] queryColumns = CollUtil.defaultIfEmpty(properties, Collections.emptyList())
            .stream()
            .map(c -> Optional.ofNullable(propertyMap.get(c)).map(TableFieldInfo::getSqlSelect).orElse(c))
            .toArray(String[]::new);

        String keyColumn;
        String keyQueryColumn;
        // 1.key is default PK
        if (CharSequenceUtil.isEmpty(keyProperty)) {
            keyProperty = tableInfo.getKeyProperty();
            keyColumn = tableInfo.getKeyColumn();
            keyQueryColumn = tableInfo.getKeyColumn();
        } else {
            // 2.key is table field
            TableFieldInfo keyFieldInfo = propertyMap.get(keyProperty);
            if (Objects.nonNull(keyFieldInfo)) {
                keyColumn = keyFieldInfo.getColumn();
                keyQueryColumn = keyFieldInfo.getSqlSelect();
            }
            // 3.key is unknown
            else {
                keyColumn = keyProperty;
                keyQueryColumn = keyProperty;
            }
        }

        // append key column if not included in query columns
        if (queryColumns.length > 0) {
            queryColumns = ArrayUtil.contains(queryColumns, keyQueryColumn) ?
                queryColumns : ArrayUtil.append(queryColumns, keyQueryColumn);
        }

        // find by namespace
        String namespace = generateContainerNamespace(info, keyColumn, queryColumns);
        return createContainer(info, keyProperty, keyColumn, queryColumns, namespace);
    }

    @Nullable
    private Container<?> createContainer(
        MapperInfo info, String keyProperty, String keyColumn, String[] queryColumns, String namespace) {
        TableInfo tableInfo = info.getTableInfo();
        MethodInvoker keyGetter = propertyOperator.findGetter(tableInfo.getEntityType(), keyProperty);
        Assert.notNull(keyGetter, "cannot find getter method of [{}] in [{}]", keyProperty, tableInfo.getEntityType());
        Container<?> container = doCreateContainer(namespace, info.getBaseMapper(), queryColumns, keyColumn, keyGetter);
        // invoke aware callback
        container = ConfigurationUtil.invokeRegisterAware(
            this, container, crane4jGlobalConfiguration.getContainerRegisterAwareList(), t -> {}
        );
        Assert.isFalse(
            Objects.equals(Container.empty(), container),
            () -> new Crane4jException("cannot resolve mybatis plus container for [{}]", namespace)
        );
        log.info("create container [{}] from mapper [{}]", namespace, info.getName());
        return container;
    }

    /**
     * Copy from {@link AbstractSqlInjector#extractModelClass}
     *
     * @param mapperClass mapper class
     * @return model class
     */
    protected Class<?> extractModelClass(Class<?> mapperClass) {
        Type[] types = mapperClass.getGenericInterfaces();
        ParameterizedType target = null;
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                Type[] typeArray = ((ParameterizedType) type).getActualTypeArguments();
                if (ArrayUtils.isNotEmpty(typeArray)) {
                    for (Type t : typeArray) {
                        if (t instanceof TypeVariable || t instanceof WildcardType) {
                            break;
                        } else {
                            target = (ParameterizedType) type;
                            break;
                        }
                    }
                }
                break;
            }
        }
        return target == null ? null : (Class<?>) target.getActualTypeArguments()[0];
    }

    /**
     * Info cache for mapper.
     */
    @Getter
    @RequiredArgsConstructor
    public static class MapperInfo {
        private final String name;
        private final BaseMapper<?> baseMapper;
        private final TableInfo tableInfo;
    }

    /**
     * Cache key.
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
