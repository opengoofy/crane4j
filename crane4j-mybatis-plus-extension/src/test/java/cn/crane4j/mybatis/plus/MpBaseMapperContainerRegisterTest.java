package cn.crane4j.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * test for {@link MpBaseMapperContainerRegister}
 *
 * @author huangchengxing
 */
@TestPropertySource(properties = "spring.config.location = classpath:test.yml")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class MpBaseMapperContainerRegisterTest {

    @Autowired
    private MpBaseMapperContainerRegister processor;
    @Autowired
    private FooMapper fooMapper;

    @SuppressWarnings("unchecked")
    @Test
    public void checkMapperInfo() {
        // check mapper info cache
        Map<String, MpBaseMapperContainerRegister.MapperInfo> infoMap = processor.mapperInfoMap;
        Assert.assertEquals(1, infoMap.size());

        // check mapper info
        MpBaseMapperContainerRegister.MapperInfo fooMapperInfo = infoMap.get("fooMapper");
        Assert.assertNotNull(fooMapperInfo);
        Assert.assertSame(fooMapper, fooMapperInfo.getBaseMapper());
        Assert.assertEquals("fooMapper", fooMapperInfo.getName());
        Assert.assertEquals(TableInfoHelper.getTableInfo(Foo.class), fooMapperInfo.getTableInfo());

        // check destroy
        processor.destroy();
        Assert.assertTrue(infoMap.isEmpty());

        // check register
        processor.registerMapper("fooMapper", fooMapper);
        MpBaseMapperContainerRegister.MapperInfo mapperInfo = infoMap.get("fooMapper");
        Assert.assertNotNull(mapperInfo);
        Assert.assertSame(fooMapper, mapperInfo.getBaseMapper());
        Assert.assertEquals("fooMapper", mapperInfo.getName());
        Assert.assertEquals(TableInfoHelper.getTableInfo(Foo.class), mapperInfo.getTableInfo());

        // check container
        Container<Object> container = (Container<Object>)processor.container("fooMapper");
        checkContainer(container, "id");
        Assert.assertSame(container, processor.container("fooMapper"));
        container = (Container<Object>)processor.container("fooMapper", Arrays.asList("age", "name"));
        checkContainer(container, "id", "age", "name", "id");
        container = (Container<Object>)processor.container("fooMapper", "name");
        checkContainer(container, "name", Arrays.asList("小红", "小明", "小刚"));
        container = (Container<Object>)processor.container("fooMapper", "id", Arrays.asList("name", "age"));
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
