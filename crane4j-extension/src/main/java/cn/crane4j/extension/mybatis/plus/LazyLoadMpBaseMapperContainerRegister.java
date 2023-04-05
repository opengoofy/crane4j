package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.reflect.PropertyOperator;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

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

    private final Function<String, BaseMapper<?>> mapperFactory;

    /**
     * Create a {@link LazyLoadMpBaseMapperContainerRegister} instance.
     *
     * @param crane4jGlobalConfiguration crane4j global configuration
     * @param propertyOperator property operator
     * @param mapperFactory mapper factory
     */
    public LazyLoadMpBaseMapperContainerRegister(
        Crane4jGlobalConfiguration crane4jGlobalConfiguration, PropertyOperator propertyOperator,
        Function<String, BaseMapper<?>> mapperFactory) {
        super(crane4jGlobalConfiguration, propertyOperator);
        this.mapperFactory = mapperFactory;
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
        if (!registerMappers.containsKey(name)) {
            synchronized (registerMappers) {
                registerMapper(name, mapperFactory.apply(name));
            }
        }
        return super.getContainer(name, keyProperty, properties);
    }
}
