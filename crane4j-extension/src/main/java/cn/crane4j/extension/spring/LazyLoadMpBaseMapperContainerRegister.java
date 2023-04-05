package cn.crane4j.extension.spring;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.extension.mybatis.plus.MpBaseMapperContainerRegister;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * <p>A implementation of {@link MpBaseMapperContainerRegister} that support lazy loading.<br />
 * When calling the {@link #getContainer} method,
 * if the Mapper interface used is not registered,
 * it will be automatically loaded from the Spring context.
 *
 * @author huangchengxing
 * @since 1.2.0
 */
public class LazyLoadMpBaseMapperContainerRegister extends MpBaseMapperContainerRegister implements DisposableBean {

    private final ApplicationContext applicationContext;

    /**
     * Create a {@link LazyLoadMpBaseMapperContainerRegister} instance.
     *
     * @param crane4jGlobalConfiguration crane4j global configuration
     * @param propertyOperator property operator
     * @param applicationContext application context
     */
    public LazyLoadMpBaseMapperContainerRegister(
        Crane4jGlobalConfiguration crane4jGlobalConfiguration, PropertyOperator propertyOperator,
        ApplicationContext applicationContext) {
        super(crane4jGlobalConfiguration, propertyOperator);
        this.applicationContext = applicationContext;
    }

    /**
     * <p>Get a container based on {@link BaseMapper#selectList}.<br />
     * When querying, the attribute name of the input parameter will be converted to
     * the table columns specified in the corresponding {@link TableField} annotation.
     *
     * @param name        mapper name
     * @param keyProperty key field name for query, if it is empty, it defaults to the field annotated by {@link TableId}
     * @param properties  fields to query, if it is empty, all table columns will be queried by default.
     * @return container
     * @see TableField
     * @see TableId
     */
    @Override
    public Container<?> getContainer(String name, @Nullable String keyProperty, @Nullable List<String> properties) {
        CacheKey cacheKey = new CacheKey(
            name, CharSequenceUtil.emptyToNull(keyProperty),
            CollUtil.defaultIfEmpty(properties, Collections.emptyList())
        );
        if (!registerMappers.containsKey(name)) {
            synchronized (registerMappers) {
                if (!registerMappers.containsKey(name)) {
                    registerMapper(name, applicationContext.getBean(name, BaseMapper.class));
                }
            }
        }
        return MapUtil.computeIfAbsent(containerCaches, cacheKey, this::doGetContainer);
    }
}
