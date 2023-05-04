package cn.crane4j.core.support.reflect;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * test for {@link ReflectMethodInvoker}
 *
 * @author huangchengxing
 */
public class ReflectMethodInvokerTest {

    private Service target;
    private Service proxy;

    @Test
    public void init() {

    }

    @Test
    public void create() {
        // Setup
        final Method method = null;

        // Run the test
        final ReflectMethodInvoker result = ReflectMethodInvoker.create("target", method, false);
        assertEquals("result", result.invoke("target", "args"));
    }

    private interface Service {
        void testMethod();
    }

    private static class ServiceImpl implements Service {
        @Override
        public void testMethod() {
        }
    }
}
