package cn.crane4j.core.util;

import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * test for {@link MethodUtils}
 *
 * @author huangchengxing
 */
public class MethodUtilsTest {

    @Test
    public void resolveParameterNames() {
        ParameterNameFinder finder = new SimpleParameterNameFinder();

        Method method1 = ReflectUtil.getMethod(MethodUtilsTest.class, "method1");
        Assert.assertNotNull(method1);
        Map<String, Parameter> parameterMap1 = MethodUtils.resolveParameterNames(finder, method1);
        Assert.assertTrue(parameterMap1.isEmpty());

        Method method2 = ReflectUtil.getMethod(MethodUtilsTest.class, "method2", String.class, String.class);
        Assert.assertNotNull(method2);
        Map<String, Parameter> parameterMap2 = MethodUtils.resolveParameterNames(finder, method2);
        Assert.assertEquals(2, parameterMap2.size());

        Map.Entry<String, Parameter> arg1 = CollUtil.get(parameterMap2.entrySet(), 0);
        Assert.assertEquals("arg0", arg1.getKey());
        Assert.assertEquals("arg0", arg1.getValue().getName());

        Map.Entry<String, Parameter> arg2 = CollUtil.get(parameterMap2.entrySet(), 1);
        Assert.assertEquals("arg1", arg2.getKey());
        Assert.assertEquals("arg1", arg2.getValue().getName());
    }

    private static void method1() {}
    private static void method2(String param1, String param2) {}
}
