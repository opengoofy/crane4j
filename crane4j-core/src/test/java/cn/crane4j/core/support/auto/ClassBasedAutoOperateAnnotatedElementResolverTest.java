package cn.crane4j.core.support.auto;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.expression.OgnlExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionEvaluator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.AnnotatedElement;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link ClassBasedAutoOperateAnnotatedElementResolver}.
 *
 * @author huangchengxing
 */
public class ClassBasedAutoOperateAnnotatedElementResolverTest {

    private AutoOperateAnnotatedElementResolver resolver;

    @Before
    public void init() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        resolver = new ClassBasedAutoOperateAnnotatedElementResolver(
            configuration, new OgnlExpressionEvaluator(), OgnlExpressionContext::new
        );
        configuration.registerContainer(LambdaContainer.<Integer>forLambda(
            "test", ids -> ids.stream().map(id -> new Foo(id, "name" + id))
                .collect(Collectors.toMap(Foo::getId, Function.identity()))
        ));
    }

    @Test
    public void testFoo1() {
        AnnotatedElement element = Foo.class;
        Assert.assertTrue(resolver.support(element, null));

        AutoOperate annotation = element.getAnnotation(AutoOperate.class);
        Assert.assertNotNull(annotation);
        AutoOperateAnnotatedElement autoOperateAnnotatedElement = resolver.resolve(element, annotation);
        Assert.assertNotNull(autoOperateAnnotatedElement);
        checkElement(element, annotation, autoOperateAnnotatedElement);

        Foo foo = new Foo(0);
        autoOperateAnnotatedElement.execute(foo);
        Assert.assertNull(foo.getName());

        foo = new Foo(1);
        autoOperateAnnotatedElement.execute(foo);
        Assert.assertEquals("name1", foo.getName());
    }

    @SuppressWarnings("all")
    private void checkElement(
        AnnotatedElement ele, AutoOperate annotation, AutoOperateAnnotatedElement element) {
        Assert.assertSame(annotation, element.getAnnotation());
        Assert.assertSame(ele, element.getElement());
        Assert.assertEquals(Foo.class, element.getBeanOperations().getSource());
    }

    @AutoOperate(condition = "id > 0")
    @AllArgsConstructor
    @RequiredArgsConstructor
    @Data
    private static class Foo {
        @Assemble(container = "test", props = @Mapping("name"), groups = "b")
        private final Integer id;
        private String name;
    }

    @AutoOperate
    @AllArgsConstructor
    @RequiredArgsConstructor
    @Data
    private static class Foo2 {
        @Assemble(container = "test", props = @Mapping("name"), groups = "b")
        private final Integer id;
        private String name;
    }
}
