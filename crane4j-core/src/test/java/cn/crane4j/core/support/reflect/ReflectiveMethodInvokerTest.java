package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.ReflectUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * test for {@link ReflectiveMethodInvoker}
 *
 * @author huangchengxing
 */
public class ReflectiveMethodInvokerTest {

    private Service target;
    private Service proxy;

    @Before
    @SuppressWarnings("all")
    public void init() {
        this.target = new ServiceImpl();
        this.proxy = (Service)Proxy.newProxyInstance(
            Service.class.getClassLoader(), new Class[]{Service.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("testMethod")) {
                        return target.testMethod((Integer)args[0], (Integer)args[1]);
                    }
                    return method.invoke(target, args);
                }
        );
    }

    @SuppressWarnings("all")
    @Test
    public void create() {
        Method method = ReflectUtils.getDeclaredMethod(Service.class, "testMethod", null);
        Assert.assertNotNull(method);

        // create by target
        ReflectiveMethodInvoker invoker = ReflectiveMethodInvoker.create(target, method, true);
        Assert.assertTrue(invoker instanceof ReflectiveMethodInvoker.InvocationMethodInvoker);
        Assert.assertSame(target, invoker.target);
        Assert.assertSame(method, invoker.method);
        Assert.assertTrue(invoker.alignArguments);
        // check invoke result
        Object result = invoker.invoke(target, 1, 2);
        Assert.assertEquals("12", result);

        // create by proxy
        invoker = ReflectiveMethodInvoker.create(proxy, method, true);
        Assert.assertTrue(invoker instanceof ReflectiveMethodInvoker.ProxyMethodInvoker);
        Assert.assertSame(Proxy.getInvocationHandler(proxy), invoker.target);
        Assert.assertSame(method, invoker.method);
        Assert.assertTrue(invoker.alignArguments);
        // check invoke result
        result = invoker.invoke(proxy, 1, 2);
        Assert.assertEquals("12", result);

        MethodInvoker invoker2 = ReflectiveMethodInvoker.create(target, method, false);
        Assert.assertThrows(RuntimeException.class, () -> invoker2.invoke(target, Boolean.TRUE));
    }

    private interface Service {
        String testMethod(Integer a, Integer b);
    }

    private static class ServiceImpl implements Service {
        @Override
        public String testMethod(Integer a, Integer b) {
            return String.valueOf(a) + b;
        }
    }
}
