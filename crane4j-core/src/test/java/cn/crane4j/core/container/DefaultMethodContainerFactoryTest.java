package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.hutool.core.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * test for {@link DefaultMethodContainerFactory}
 *
 * @author huangchengxing
 */
public class DefaultMethodContainerFactoryTest {

    private DefaultMethodContainerFactory factory;
    private Service service;
    private final Foo foo1 = new Foo("1", "foo");
    private final Foo foo2 = new Foo("2", "foo");

    private Method noneResultMethod;
    private Method mappedMethod;
    private Method onoToOneMethod;
    private Method oneToManyMethod;

    @Before
    public void initMethod() {
        factory = new DefaultMethodContainerFactory(
            new ReflectPropertyOperator(), new SimpleAnnotationFinder()
        );
        service = new Service();
        noneResultMethod = ReflectUtil.getMethod(Service.class, "noneResultMethod");
        Assert.assertNotNull(noneResultMethod);
        mappedMethod = ReflectUtil.getMethod(Service.class, "mappedMethod", List.class);
        Assert.assertNotNull(mappedMethod);
        onoToOneMethod = ReflectUtil.getMethod(Service.class, "onoToOneMethod", List.class);
        Assert.assertNotNull(onoToOneMethod);
        oneToManyMethod = ReflectUtil.getMethod(Service.class, "oneToManyMethod", List.class);
        Assert.assertNotNull(oneToManyMethod);
    }

    @Test
    public void getSort() {
        Assert.assertEquals(DefaultMethodContainerFactory.ORDER, factory.getSort());
    }

    @Test
    public void support() {
        Assert.assertFalse(factory.support(service, noneResultMethod));
        Assert.assertTrue(factory.support(service, mappedMethod));
        Assert.assertTrue(factory.support(service, onoToOneMethod));
        Assert.assertTrue(factory.support(service, oneToManyMethod));
    }

    @Test
    public void getWhenMappedMethod() {
        List<Container<Object>> containers = factory.get(service, mappedMethod);
        Assert.assertEquals(1, containers.size());
        Container<Object> container = containers.get(0);
        Assert.assertNotNull(container);

        Assert.assertEquals("mappedMethod", container.getNamespace());
        Map<Object, ?> data = container.get(null);
        Assert.assertEquals(foo1, data.get(foo1.id));
        Assert.assertEquals(foo2, data.get(foo2.id));
    }

    @Test
    public void getWhenOnoToOneMethod() {
        List<Container<Object>> containers = factory.get(service, onoToOneMethod);
        Assert.assertEquals(1, containers.size());
        Container<Object> container = containers.get(0);
        Assert.assertNotNull(container);

        Assert.assertEquals("onoToOneMethod", container.getNamespace());
        Map<Object, ?> data = container.get(null);
        Assert.assertEquals(foo1, data.get(foo1.id));
        Assert.assertEquals(foo2, data.get(foo2.id));
    }

    @Test
    public void getWhenOneToManyMethod() {
        List<Container<Object>> containers = factory.get(service, oneToManyMethod);
        Assert.assertEquals(1, containers.size());
        Container<Object> container = containers.get(0);
        Assert.assertNotNull(container);

        Assert.assertEquals("oneToManyMethod", container.getNamespace());
        Map<Object, ?> data = container.get(null);
        Assert.assertEquals(Arrays.asList(foo1, foo2), data.get(foo1.name));
    }

    private class Service {
        @ContainerMethod(namespace = "noneResultMethod", type = MappingType.MAPPED, resultType = Foo.class)
        public void noneResultMethod() { }

        @ContainerMethod(namespace = "mappedMethod", type = MappingType.MAPPED, resultType = Foo.class)
        public Map<String, Foo> mappedMethod(List<String> args) {
            return Stream.of(foo1, foo2).collect(Collectors.toMap(Foo::getId, Function.identity()));
        }

        @ContainerMethod(namespace = "onoToOneMethod", type = MappingType.ONE_TO_ONE, resultType = Foo.class)
        public Set<Foo> onoToOneMethod(List<String> args) {
            return Stream.of(foo1, foo2).collect(Collectors.toSet());
        }

        @ContainerMethod(namespace = "oneToManyMethod", type = MappingType.ONE_TO_MANY, resultType = Foo.class, resultKey = "name")
        public List<Foo> oneToManyMethod(List<String> args) {
            return Arrays.asList(foo1, foo2);
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    private static class Foo {
        private String id;
        private String name;
    }
}
