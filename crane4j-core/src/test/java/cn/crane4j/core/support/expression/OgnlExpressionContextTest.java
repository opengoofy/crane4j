package cn.crane4j.core.support.expression;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * test for {@link OgnlExpressionContext}
 *
 * @author huangchengxing
 */
public class OgnlExpressionContextTest {

    @Test
    public void test() {
        Object root = new Object();
        OgnlExpressionContext context = new OgnlExpressionContext(root);
        Assert.assertSame(root, context.getRoot());

        // 根对象
        root = new Object();
        context.setRoot(root);
        Assert.assertSame(root, context.getRoot());

        // 参数注册
        context.registerVariable("var", "var");
        Map<String, Object> vars = context.getVariables();
        Assert.assertNotNull(vars);
        Assert.assertEquals("var", context.getVariables().get("var"));

        // 拷贝构造器
        OgnlExpressionContext copy = new OgnlExpressionContext(context);
        Assert.assertSame(context.getRoot(), copy.getRoot());
        Assert.assertEquals(context.getVariables(), copy.getVariables());
    }
}
