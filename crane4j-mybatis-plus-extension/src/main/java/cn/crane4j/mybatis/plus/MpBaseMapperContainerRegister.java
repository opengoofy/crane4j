package cn.crane4j.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ConfigurationUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import java.util.Collection;
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
@Slf4j
@RequiredArgsConstructor
public class MpBaseMapperContainerRegister implements DisposableBean {

    protected final ApplicationContext applicationContext;
    protected final Crane4jGlobalConfiguration crane4jGlobalConfiguration;
    protected final Map<String, MapperInfo> mapperInfoMap = new HashMap<>(32);
    protected final PropertyOperator propertyOperator;
    private final Map<String, Container<?>> containerCaches = new ConcurrentHashMap<>(32);

    // ================== public ==================

    /**
     * Register mapper.
     *
     * @param mapperName mapper named
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
    public Container<?> getContainer(String mapperName, String keyProperty, List<String> properties) {
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
        Container<?> container = createContainer(info, keyProperty, keyColumn, queryColumns, namespace);
        Assert.isFalse(
            Objects.equals(Container.empty(), container),
            () -> new Crane4jException("cannot resolve mybatis plus container for [{}]", namespace)
        );
        return container;
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

    private Container<?> createContainer(
        MapperInfo info, String keyProperty, String keyColumn, String[] queryColumns, String namespace) {
        return MapUtil.computeIfAbsent(containerCaches, namespace, ns -> {
            TableInfo tableInfo = info.getTableInfo();
            MethodInvoker keyGetter = propertyOperator.findGetter(tableInfo.getEntityType(), keyProperty);
            Assert.notNull(keyGetter, "cannot find getter method of [{}] in [{}]", keyProperty, tableInfo.getEntityType());
            Container<?> container = doCreateContainer(namespace, info.getBaseMapper(), queryColumns, keyColumn, keyGetter);
            // invoke aware callback
            Collection<ContainerRegisterAware> awareList = crane4jGlobalConfiguration.getContainerRegisterAwareList();
            Container<?> actual = ConfigurationUtil.invokeBeforeContainerRegister(null, container, awareList);
            ConfigurationUtil.invokeAfterContainerRegister(this, actual, awareList);
            log.info("create container [{}] from mapper [{}]", namespace, info.getName());
            return Objects.isNull(actual) ? Container.empty() : actual;
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
