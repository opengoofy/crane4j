package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.ReflectUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link AutoOperateAnnotatedElementResolver}.
 *
 * @author huangchengxing
 */
public class AutoOperateAnnotatedElementResolverTest {

    private Crane4jGlobalConfiguration configuration;
    private AutoOperateAnnotatedElementResolver resolver;

    @Before
    public void init() {
        configuration = SimpleCrane4jGlobalConfiguration.create();
        resolver = new AutoOperateAnnotatedElementResolver(configuration);
        configuration.registerContainer(LambdaContainer.<Integer>forLambda(
            "test", ids -> ids.stream().map(id -> new Foo(id, "name" + id))
                .collect(Collectors.toMap(Foo::getId, Function.identity()))
        ));
    }

    @Test
    public void resolveMethod() {
        Method method = ReflectUtils.getMethod(this.getClass(), "method", Collection.class);
        Assert.assertNotNull(method);
        AutoOperate annotation = method.getAnnotation(AutoOperate.class);
        Assert.assertNotNull(annotation);
        AutoOperateAnnotatedElement element = resolver.resolve(method, annotation);

        checkElement(method, annotation, element);

        Result<Foo> foo = new Result<>(new Foo(1));
        element.execute(foo);
        Assert.assertEquals("name1", foo.getData().getName());

        Assert.assertThrows(Crane4jException.class, () -> resolver.resolve(Object.class, annotation));
    }

    @Test
    public void resolveParameter() {
        Method method = ReflectUtils.getMethod(this.getClass(), "method2", Result.class);
        Assert.assertNotNull(method);
        Parameter parameter = method.getParameters()[0];
        AutoOperate annotation = parameter.getAnnotation(AutoOperate.class);
        Assert.assertNotNull(annotation);
        AutoOperateAnnotatedElement element = resolver.resolve(parameter, annotation);

        checkElement(parameter, annotation, element);

        Result<Foo> foo = new Result<>(new Foo(1));
        element.execute(foo);
        Assert.assertEquals("name1", foo.getData().getName());
    }

    @Test
    public void resolveOther() {
        Assert.assertThrows(Crane4jException.class, () -> resolver.resolve(Object.class, null));
    }

    private void checkElement(AnnotatedElement ele, AutoOperate annotation, AutoOperateAnnotatedElement element) {
        Assert.assertSame(annotation, element.getAnnotation());
        Assert.assertSame(ele, element.getElement());
        Assert.assertEquals(Foo.class, element.getBeanOperations().getSource());
    }

    @AutoOperate(type = Foo.class, includes = {"a", "b", "c"}, excludes = "c", on = "data")
    private Result<Collection<Foo>> method(Collection<Integer> ids) {
        return new Result<>(ids.stream().map(Foo::new).collect(Collectors.toList()));
    }

    private Result<Collection<Foo>> method2(
        @AutoOperate(type = Foo.class, includes = {"a", "b", "c"}, excludes = "c", on = "data")
        Result<Collection<Foo>> result) {
        return result;
    }

    @Getter
    @RequiredArgsConstructor
    private static class Result<T> {
        private final T data;
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    @Data
    private static class Foo {
        @Assemble(container = "test", props = @Mapping("name"), groups = "b")
        private final Integer id;
        private String name;
    }
}
