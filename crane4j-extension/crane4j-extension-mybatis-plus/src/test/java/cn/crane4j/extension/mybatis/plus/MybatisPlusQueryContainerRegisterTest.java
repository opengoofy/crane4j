package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.container.AbstractQueryContainerCreator;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.core.util.ArrayUtils;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * test for {@link MybatisPlusQueryContainerRegister}
 *
 * @author huangchengxing
 */
public class MybatisPlusQueryContainerRegisterTest extends MpBaseTest {

    private MybatisPlusQueryContainerRegister mybatisPlusQueryContainerRegister;

    @Override
    public void afterInit() {
        Crane4jGlobalConfiguration crane4jGlobalConfiguration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        mybatisPlusQueryContainerRegister = new LazyLoadMybatisPlusQueryContainerRegister(
            new MethodInvokerContainerCreator(new ReflectPropertyOperator()),
            crane4jGlobalConfiguration, name -> fooMapper
        );
        mybatisPlusQueryContainerRegister.registerRepository("fooMapper", fooMapper);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void checkMapperInfo() {
        // check mapper info cache
        Map<String, AbstractQueryContainerCreator.Repository<BaseMapper<?>>> infoMap
            = mybatisPlusQueryContainerRegister.getRegisterRepositories();
        Assert.assertEquals(1, infoMap.size());

        // check mapper info
        AbstractQueryContainerCreator.Repository<BaseMapper<?>> fooMapperInfo = infoMap.get("fooMapper");
        Assert.assertNotNull(fooMapperInfo);
        Assert.assertSame(fooMapper, fooMapperInfo.getTarget());

        // check destroy
        mybatisPlusQueryContainerRegister.destroy();
        Assert.assertTrue(infoMap.isEmpty());

        // check container
        Container<Object> container = (Container<Object>)mybatisPlusQueryContainerRegister.getContainer("fooMapper", null, null);

        // check lazy load
        AbstractQueryContainerCreator.Repository<BaseMapper<?>> mapperInfo = infoMap.get("fooMapper");
        Assert.assertNotNull(mapperInfo);
        Assert.assertSame(fooMapper, mapperInfo.getTarget());

        checkContainer(container, "id");
        Assert.assertSame(container, mybatisPlusQueryContainerRegister.getContainer("fooMapper", null, null));
        container = (Container<Object>)mybatisPlusQueryContainerRegister.getContainer("fooMapper", null, Arrays.asList("age", "name"));
        checkContainer(container, "id", "age", "name", "id");
        container = (Container<Object>)mybatisPlusQueryContainerRegister.getContainer("fooMapper", "userName", null);
        checkContainer(container, "name", Arrays.asList("小红", "小明", "小刚"));
        container = (Container<Object>)mybatisPlusQueryContainerRegister.getContainer("fooMapper", "id", Arrays.asList("name", "age"));
        checkContainer(container, "id", "name", "age");
    }

    private void checkContainer(Container<Object> container, String keyColumn, String... queryColumns) {
        checkContainer(container, keyColumn, Arrays.asList(1, 2, 3), queryColumns);
    }

    private void checkContainer(Container<Object> container, String keyColumn, List<Object> keys, String... queryColumns) {
        Assert.assertNotNull(container);
        Assert.assertTrue(container instanceof MethodInvokerContainer);

        if (ArrayUtils.length(queryColumns) > 0 && !ArrayUtils.contains(queryColumns, keyColumn)) {
            queryColumns = ArrayUtils.append(queryColumns, keyColumn);
        }

        String namespace = CharSequenceUtil.format(
            "select {} from foo where {} in ?",
            ArrayUtils.isEmpty(queryColumns) ? "*" : ArrayUtils.join(queryColumns, ", "), keyColumn
        );
        Assert.assertEquals(namespace, container.getNamespace());

        @SuppressWarnings("unchecked")
        Map<Object, Foo> sources = (Map<Object, Foo>)container.get(keys);
        Assert.assertEquals(3, sources.size());
    }
}
