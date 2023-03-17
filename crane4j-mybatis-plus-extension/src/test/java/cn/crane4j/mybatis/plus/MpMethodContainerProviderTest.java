package cn.crane4j.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.Crane4jException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * test for {@link MpMethodContainerProvider}.
 *
 * @author huangchengxing
 */
@TestPropertySource(properties = "spring.config.location = classpath:test.yml")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class MpMethodContainerProviderTest {

    @Autowired
    private MpMethodContainerProvider provider;

    @Test
    public void getContainer() {
        Assert.assertThrows(Crane4jException.class, () -> provider.getContainer("nothing"));

        Container<?> container = provider.getContainer("container('fooMapper', 'userName', {'userAge', 'userSex'})");
        Assert.assertNotNull(container);
        checkNamespace(container.getNamespace(), "name", "age AS userAge", "sex AS userSex", "name AS userName");

        container = provider.getContainer("container('fooMapper', {'userAge', 'userSex'})");
        Assert.assertNotNull(container);
        checkNamespace(container.getNamespace(), "id", "age AS userAge", "sex AS userSex", "id");

        container = provider.getContainer("container('fooMapper', 'userName')");
        Assert.assertNotNull(container);
        checkNamespace(container.getNamespace(), "name");

        container = provider.getContainer("container('fooMapper')");
        Assert.assertNotNull(container);
        checkNamespace(container.getNamespace(), "id");
    }

    private void checkNamespace(String namespace, String keyColumn, String... queryColumns) {
        String expected = CharSequenceUtil.format(
            "select {} from foo where {} in ?",
            ArrayUtil.isEmpty(queryColumns) ? "*" : ArrayUtil.join(queryColumns, ", "), keyColumn
        );
        Assert.assertEquals(expected, namespace);
    }
}
