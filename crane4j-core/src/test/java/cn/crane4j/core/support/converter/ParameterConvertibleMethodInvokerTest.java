package cn.crane4j.core.support.converter;

import cn.crane4j.core.support.MethodInvoker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link ParameterConvertibleMethodInvoker}
 *
 * @author huangchengxing
 */
public class ParameterConvertibleMethodInvokerTest {

    private ConverterManager converterManager;
    private MethodInvoker invoker;
    private Class<?>[] parameterTypes;

    @Before
    public void init() {
        invoker = (t, args) -> {
            String a = (String) args[0];
            String b = (String) args[1];
            return a + b;
        };
        parameterTypes = new Class<?>[]{String.class, String.class};
        converterManager = new SimpleConverterManager();
    }

    @Test
    public void create() {
        // if no parameter, return original invoker
        MethodInvoker methodInvoker = ParameterConvertibleMethodInvoker.create(invoker, converterManager, new Class<?>[0]);
        Assert.assertSame(invoker, methodInvoker);
        // has parameter, but args is empty
        methodInvoker = ParameterConvertibleMethodInvoker.create(invoker, converterManager, parameterTypes);
        Object result = methodInvoker.invoke(null, new String[0]);
        Assert.assertEquals("nullnull", result);

        // has parameter, and args is greater than parameter count
        result = methodInvoker.invoke(null, new String[]{"a", "b", "c"});
        Assert.assertEquals("ab", result);

        // has parameter, and args is less than parameter count
        result = methodInvoker.invoke(null, new String[]{"a"});
        Assert.assertEquals("anull", result);
    }
}
