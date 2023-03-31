package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * test for {@link MpBaseMapperContainerRegister}
 *
 * @author huangchengxing
 */
public class MpBaseMapperContainerRegisterTest extends MpBaseTest {

    private MpBaseMapperContainerRegister mapperContainerRegister;

    @Override
    public void afterInit() {
        Crane4jGlobalConfiguration crane4jGlobalConfiguration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        mapperContainerRegister = new MpBaseMapperContainerRegister(crane4jGlobalConfiguration, new ReflectPropertyOperator());
        mapperContainerRegister.registerMapper("fooMapper", fooMapper);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void checkMapperInfo() {
        // check mapper info cache
        Map<String, MpBaseMapperContainerRegister.MapperInfo> infoMap = mapperContainerRegister.registerMappers;
        Assert.assertEquals(1, infoMap.size());

        // check mapper info
        MpBaseMapperContainerRegister.MapperInfo fooMapperInfo = infoMap.get("fooMapper");
        Assert.assertNotNull(fooMapperInfo);
        Assert.assertSame(fooMapper, fooMapperInfo.getBaseMapper());
        Assert.assertEquals("fooMapper", fooMapperInfo.getName());
        Assert.assertEquals(TableInfoHelper.getTableInfo(Foo.class), fooMapperInfo.getTableInfo());

        // check destroy
        mapperContainerRegister.destroy();
        Assert.assertTrue(infoMap.isEmpty());

        // check register
        mapperContainerRegister.registerMapper("fooMapper", fooMapper);
        MpBaseMapperContainerRegister.MapperInfo mapperInfo = infoMap.get("fooMapper");
        Assert.assertNotNull(mapperInfo);
        Assert.assertSame(fooMapper, mapperInfo.getBaseMapper());
        Assert.assertEquals("fooMapper", mapperInfo.getName());
        Assert.assertEquals(TableInfoHelper.getTableInfo(Foo.class), mapperInfo.getTableInfo());

        // check container
        Container<Object> container = (Container<Object>)mapperContainerRegister.getContainer("fooMapper", null, null);
        checkContainer(container, "id");
        Assert.assertSame(container, mapperContainerRegister.getContainer("fooMapper", null, null));
        container = (Container<Object>)mapperContainerRegister.getContainer("fooMapper", null, Arrays.asList("age", "name"));
        checkContainer(container, "id", "age", "name", "id");
        container = (Container<Object>)mapperContainerRegister.getContainer("fooMapper", "userName", null);
        checkContainer(container, "name", Arrays.asList("小红", "小明", "小刚"));
        container = (Container<Object>)mapperContainerRegister.getContainer("fooMapper", "id", Arrays.asList("name", "age"));
        checkContainer(container, "id", "name", "age");
    }

    private void checkContainer(Container<Object> container, String keyColumn, String... queryColumns) {
        checkContainer(container, keyColumn, Arrays.asList(1, 2, 3), queryColumns);
    }

    private void checkContainer(Container<Object> container, String keyColumn, List<Object> keys, String... queryColumns) {
        Assert.assertNotNull(container);
        Assert.assertTrue(container instanceof MpMethodContainer);

        if (ArrayUtil.length(queryColumns) > 0 && !ArrayUtil.contains(queryColumns, keyColumn)) {
            queryColumns = ArrayUtil.append(queryColumns, keyColumn);
        }

        String namespace = CharSequenceUtil.format(
            "select {} from foo where {} in ?",
            ArrayUtil.isEmpty(queryColumns) ? "*" : ArrayUtil.join(queryColumns, ", "), keyColumn
        );
        Assert.assertEquals(namespace, container.getNamespace());

        @SuppressWarnings("unchecked")
        Map<Object, Foo> sources = (Map<Object, Foo>)container.get(keys);
        Assert.assertEquals(3, sources.size());
    }
}
