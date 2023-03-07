package cn.crane4j.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>A processor and register of MybatisPlus {@link BaseMapper}.
 * After the Spring context is initialized, scan the {@link BaseMapper} in the context,
 * And adapt its {@link BaseMapper#selectList} method to
 * the container according to the configuration.
 *
 * @author huangchengxing
 * @see MpBaseMapperContainerAutoRegistrar
 * @see MpMethodContainer
 */
@RequiredArgsConstructor
public class MpBaseMapperContainerRegister implements DisposableBean {

    protected final ApplicationContext applicationContext;
    protected final Map<String, MapperInfo> mapperInfoMap = new HashMap<>(32);
    protected final PropertyOperator propertyOperator;
    private final Map<String, Container<?>> containerCaches = new ConcurrentHashMap<>(32);

    // ================== public ==================

    /**
     * Register mapper.
     *
     * @param mapperName mapper name
     * @param baseMapper baseMapper
     */
    public final void registerMapper(String mapperName, BaseMapper<?> baseMapper) {
        Objects.requireNonNull(mapperName);
        Objects.requireNonNull(baseMapper);
        mapperInfoMap.computeIfAbsent(mapperName, name -> {
            Class<?> beanType = ResolvableType.forClass(
                BaseMapper.class, baseMapper.getClass()
            ).getGeneric(0).getRawClass();
            Assert.notNull(beanType, "cannot resolve bean type of mapper [{}]", mapperName);
            TableInfo tableInfo = TableInfoHelper.getTableInfo(beanType);
            return new MapperInfo(name, baseMapper, tableInfo);
        });
    }

    /**
     * Get a container based on {@link BaseMapper#selectList}.
     *
     * @param mapperName mapper name
     * @param keyProperty key field name for query
     * @return cn.crane4j.core.container.Container<?>
     */
    public Container<?> container(String mapperName, String keyProperty) {
        return container(mapperName, keyProperty, null);
    }

    /**
     * Get a container based on {@link BaseMapper#selectList}.
     *
     * @param mapperName mapper name
     * @return container
     */
    public Container<?> container(String mapperName) {
        return container(mapperName, null, null);
    }

    /**
     * Get a container based on {@link BaseMapper#selectList}.
     *
     * @param mapperName mapper name
     * @param properties fields to query
     * @return container
     */
    public Container<?> container(String mapperName, List<String> properties) {
        return container(mapperName, null, properties);
    }

    /**
     * Get a container based on {@link BaseMapper#selectList},
     * When querying, the attribute name of the input parameter will be converted to
     * the table columns specified in the corresponding {@link TableField} annotation.
     *
     * @param mapperName mapper name
     * @param keyProperty key field name for query, if it is empty, it defaults to the field annotated by {@link TableId}
     * @param properties fields to query, if it is empty, all table columns will be queried by default.
     * @return container
     * @see TableField
     * @see TableId
     */
    public Container<?> container(
        String mapperName, @Nullable String keyProperty, @Nullable List<String> properties) {
        return getContainer(mapperName, keyProperty, properties);
    }

    // ================== private ==================

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
     * Create {@link Container}
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

    private Container<?> getContainer(String mapperName, String keyProperty, List<String> properties) {
        MapperInfo info = mapperInfoMap.get(mapperName);
        Assert.notNull(info, "cannot find mapper [{}]", mapperName);
        TableInfo tableInfo = info.getTableInfo();

        // resolve query columns
        Map<String, String> propertyMap = tableInfo.getFieldList().stream()
            .collect(Collectors.toMap(f -> f.getField().getName(), TableFieldInfo::getColumn));
        String[] queryColumns = CollUtil.defaultIfEmpty(properties, Collections.emptyList())
            .stream().map(c -> propertyMap.getOrDefault(c, c)).toArray(String[]::new);

        // resolve key column
        String keyColumn = propertyMap.getOrDefault(keyProperty, keyProperty);
        if (CharSequenceUtil.isEmpty(keyProperty)) {
            keyProperty = tableInfo.getKeyProperty();
            keyColumn = tableInfo.getKeyColumn();
        }

        // append key column if not included in query columns
        if (queryColumns.length > 0) {
            queryColumns = ArrayUtil.contains(queryColumns, keyColumn) ?
                queryColumns : ArrayUtil.append(queryColumns, keyColumn);
        }

        // find by namespace
        String namespace = generateContainerNamespace(info, keyColumn, queryColumns);
        return createContainer(info, keyProperty, keyColumn, queryColumns, namespace);
    }

    private Container<?> createContainer(
        MapperInfo info, String keyProperty, String keyColumn, String[] queryColumns, String namespace) {
        return MapUtil.computeIfAbsent(containerCaches, namespace, ns -> {
            TableInfo tableInfo = info.getTableInfo();
            MethodInvoker keyGetter = propertyOperator.findGetter(tableInfo.getEntityType(), keyProperty);
            Assert.notNull(keyGetter, "cannot find getter method of [{}] in [{}]", keyProperty, tableInfo.getEntityType());
            return doCreateContainer(namespace, info.getBaseMapper(), queryColumns, keyColumn, keyGetter);
        });
    }

    /**
     * Invoked by the containing {@code BeanFactory} on destruction of a bean.
     *
     */
    @Override
    public void destroy() {
        mapperInfoMap.clear();
        containerCaches.clear();
    }

    /**
     * Info cache for mapper.
     */
    @Getter
    @RequiredArgsConstructor
    protected static class MapperInfo {
        private final String name;
        private final BaseMapper<?> baseMapper;
        private final TableInfo tableInfo;
    }
}
