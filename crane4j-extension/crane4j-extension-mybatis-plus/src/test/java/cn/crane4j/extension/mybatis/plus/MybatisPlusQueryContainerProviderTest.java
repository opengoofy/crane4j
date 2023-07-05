package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.container.query.AbstractQueryContainerProvider;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

/**
 * test for {@link MybatisPlusQueryContainerProvider}
 *
 * @author huangchengxing
 */
public class MybatisPlusQueryContainerProviderTest extends MpBaseTest {

    private MybatisPlusQueryContainerProvider mybatisPlusQueryContainerProvider;

    @Override
    public void afterInit() {
        Crane4jGlobalConfiguration crane4jGlobalConfiguration = SimpleCrane4jGlobalConfiguration.create();
        ConverterManager converterManager = new HutoolConverterManager();
        mybatisPlusQueryContainerProvider = new MybatisPlusQueryContainerProvider(
            new MethodInvokerContainerCreator(new ReflectivePropertyOperator(converterManager), converterManager),
            crane4jGlobalConfiguration, name -> fooMapper
        );
        mybatisPlusQueryContainerProvider.registerRepository("fooMapper", fooMapper);
    }

    @Test
    public void checkMapperInfo() {
        // check mapper info cache
        Map<String, AbstractQueryContainerProvider.Repository<BaseMapper<?>>> infoMap
            = mybatisPlusQueryContainerProvider.getRegisteredRepositories();
        Assert.assertEquals(1, infoMap.size());

        // check mapper info
        AbstractQueryContainerProvider.Repository<BaseMapper<?>> fooMapperInfo = infoMap.get("fooMapper");
        Assert.assertNotNull(fooMapperInfo);
        Assert.assertSame(fooMapper, fooMapperInfo.getTarget());

        // check destroy
        mybatisPlusQueryContainerProvider.destroy();
        Assert.assertTrue(infoMap.isEmpty());

        // check container
        Container<Object> container = mybatisPlusQueryContainerProvider.getQueryContainer("fooMapper", null, null);

        // check lazy load
        AbstractQueryContainerProvider.Repository<BaseMapper<?>> mapperInfo = infoMap.get("fooMapper");
        Assert.assertNotNull(mapperInfo);
        Assert.assertSame(fooMapper, mapperInfo.getTarget());

        checkContainer(container);
        Assert.assertNotSame(container, mybatisPlusQueryContainerProvider.getQueryContainer("fooMapper", null, null));
        container = mybatisPlusQueryContainerProvider.getQueryContainer("fooMapper", null, Arrays.asList("age", "name"));
        checkContainer(container);
        container = mybatisPlusQueryContainerProvider.getQueryContainer("fooMapper", "userName", null);
        checkContainer(container);
        container = mybatisPlusQueryContainerProvider.getQueryContainer("fooMapper", "id", Arrays.asList("name", "age"));
        checkContainer(container);
    }

    private void checkContainer(Container<Object> container) {
        Assert.assertNotNull(container);
        Assert.assertTrue(container instanceof MethodInvokerContainer);
    }
}
