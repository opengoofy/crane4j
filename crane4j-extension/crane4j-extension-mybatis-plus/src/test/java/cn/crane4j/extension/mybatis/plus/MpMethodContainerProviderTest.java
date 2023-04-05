package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.expression.ExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionEvaluator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link MpMethodContainerProvider}.
 *
 * @author huangchengxing
 */
public class MpMethodContainerProviderTest extends MpBaseTest {

    private MpMethodContainerProvider provider;

    @Override
    public void afterInit() {
        Crane4jGlobalConfiguration crane4jGlobalConfiguration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        MpBaseMapperContainerRegister mapperContainerRegister = new MpBaseMapperContainerRegister(crane4jGlobalConfiguration, new ReflectPropertyOperator());
        mapperContainerRegister.registerMapper("fooMapper", fooMapper);
        provider = new MpMethodContainerProvider(mapperContainerRegister, new OgnlExpressionEvaluator(), provider -> {
            ExpressionContext context = new OgnlExpressionContext();
            context.setRoot(provider);
            return context;
        });
    }

    @Test
    public void getContainer() {
        Assert.assertThrows(Crane4jException.class, () -> provider.getContainer("#?"));

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
