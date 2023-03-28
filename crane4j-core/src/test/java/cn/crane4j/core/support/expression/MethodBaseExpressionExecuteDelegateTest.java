package cn.crane4j.core.support.expression;

import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.hutool.core.util.ReflectUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * test for {@link MethodBaseExpressionExecuteDelegate}.
 *
 * @author huangchengxing
 */
public class MethodBaseExpressionExecuteDelegateTest {

    private MethodBaseExpressionExecuteDelegate expressionExecuteDelegate;

    @Before
    public void init() {
        expressionExecuteDelegate = new MethodBaseExpressionExecuteDelegate(
            new SimpleParameterNameFinder(), new OgnlExpressionEvaluator(), method -> new OgnlExpressionContext()
        );
    }

    @Test
    public void execute() {
        Method method = ReflectUtil.getMethod(
            MethodBaseExpressionExecuteDelegateTest.class,
            "method", Integer.class, Integer.class
        );
        Assert.assertNotNull(method);
        Integer result = expressionExecuteDelegate.execute(
            "arg0 + arg1 + result", Integer.class,
            method, new Object[]{ 1, 2 }, 3
        );
        Assert.assertEquals((Integer)6, result);

        method = ReflectUtil.getMethod(MethodBaseExpressionExecuteDelegateTest.class, "method2");
        Assert.assertNotNull(method);
        result = expressionExecuteDelegate.execute(
            "a0 + a1 + result", Integer.class,
            method, new Object[]{ 1, 2 }, 3
        );
        Assert.assertEquals((Integer)6, result);
    }

    public static Integer method(Integer p1, Integer p2) {
        return p1 + p2;
    }

    public static void method2() {
    }
}
