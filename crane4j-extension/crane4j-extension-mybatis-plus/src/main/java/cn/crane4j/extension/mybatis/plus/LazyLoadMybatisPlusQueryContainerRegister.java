package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * <p>A implementation of {@link MybatisPlusQueryContainerRegister} that support lazy loading.<br />
 * When calling the {@link #getContainer} method, if the Mapper interface used is not registered,
 * it will be automatically loaded from the Spring context.
 *
 * @author huangchengxing
 */
public class LazyLoadMybatisPlusQueryContainerRegister extends MybatisPlusQueryContainerRegister {

    private final Function<String, BaseMapper<?>> mapperFactory;

    /**
     * Create a {@link MybatisPlusQueryContainerRegister} instance
     *
     * @param methodInvokerContainerCreator method invoker container creator
     * @param globalConfiguration           global configuration
     */
    public LazyLoadMybatisPlusQueryContainerRegister(
        MethodInvokerContainerCreator methodInvokerContainerCreator,
        Crane4jGlobalConfiguration globalConfiguration, Function<String, BaseMapper<?>> mapperFactory) {
        super(methodInvokerContainerCreator, globalConfiguration);
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
        if (!registerRepositories.containsKey(name)) {
            synchronized (registerRepositories) {
                super.registerRepository(name, mapperFactory.apply(name));
            }
        }
        return super.getContainer(name, keyProperty, properties);
    }
}
