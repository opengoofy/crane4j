package cn.crane4j.springboot.support;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.aop.AutoOperateMethodAnnotatedElementResolver;
import cn.crane4j.springboot.config.Crane4jAutoConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * test for {@link AutoOperateMethodAnnotatedElementResolver}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Crane4jAutoConfiguration.class)
public class AutoOperateMethodAnnotatedElementResolverTest {

    @Autowired
    private Crane4jGlobalConfiguration applicationContext;
    private AutoOperateMethodAnnotatedElementResolver support;

    @Before
    public void init() {
        support = new AutoOperateMethodAnnotatedElementResolver(
            applicationContext
        );
    }

    @Test
    public void resolveElement() {
        Method noneMethod = ReflectionUtils.findMethod(getClass(), "compute");
        Assert.assertNotNull(noneMethod);
        AutoOperate noneAnnotation = noneMethod.getAnnotation(AutoOperate.class);
        Assert.assertThrows(NullPointerException.class, () -> support.resolve(noneMethod, noneAnnotation));

        Method method = getMethod();
        AutoOperate annotation = method.getAnnotation(AutoOperate.class);
        AutoOperateMethodAnnotatedElementResolver.ResolvedElement element = support.resolve(method, annotation);
        Assert.assertEquals(method, element.getElement());
        Assert.assertSame(2, element.getExtractor().invoke(new Foo(2)));
        Assert.assertEquals(1, element.getGroups().size());
        Assert.assertTrue(element.getGroups().contains("a"));
        Assert.assertEquals(Foo.class, element.getBeanOperations().getTargetType());
        Assert.assertEquals(DisorderedBeanOperationExecutor.class, element.getExecutor().getClass());
        element.execute(new Foo(2));
    }

    private Method getMethod() {
        Method method = ReflectionUtils.findMethod(getClass(), "compute", Integer.class, Integer.class);
        Assert.assertNotNull(method);
        return method;
    }

    @AutoOperate(type = Foo.class, includes = {"a", "b"}, excludes = {"b", "c"}, condition = "'true'", on = "total")
    private Foo compute(Integer a, Integer b) {
        return new Foo(a + b);
    }
    @AutoOperate(type = Foo.class, includes = {"a", "b"}, excludes = {"b", "c"}, condition = "'true'", on = "none")
    private Foo compute() {
        return new Foo(0);
    }

    @Getter
    @RequiredArgsConstructor
    private static class Foo {
        private final Integer total;
    }
}
