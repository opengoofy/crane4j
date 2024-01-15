package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.ReflectUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
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
    private ServiceImpl serviceImpl;
    private Service proxy;
    private final Foo foo1 = new Foo("1", "foo");
    private final Foo foo2 = new Foo("2", "foo");

    private Method noneMappedMethod;
    private Method noneResultMethod;
    private Method mappedMethod;
    private Method onoToOneMethod;
    private Method oneToManyMethod;

    @Before
    public void initMethod() {
        ConverterManager converterManager = new HutoolConverterManager();
        MethodInvokerContainerCreator containerCreator = new MethodInvokerContainerCreator(
            new ReflectivePropertyOperator(converterManager), converterManager
        );
        factory = new DefaultMethodContainerFactory(
            containerCreator, new SimpleAnnotationFinder()
        );
        serviceImpl = new ServiceImpl();
        noneMappedMethod = ReflectUtils.getMethod(ServiceImpl.class, "noneMappedMethod", String.class);
        Assert.assertNotNull(noneMappedMethod);
        noneResultMethod = ReflectUtils.getMethod(ServiceImpl.class, "noneResultMethod");
        Assert.assertNotNull(noneResultMethod);
        mappedMethod = ReflectUtils.getMethod(ServiceImpl.class, "mappedMethod", List.class);
        Assert.assertNotNull(mappedMethod);
        onoToOneMethod = ReflectUtils.getMethod(ServiceImpl.class, "onoToOneMethod", List.class);
        Assert.assertNotNull(onoToOneMethod);
        oneToManyMethod = ReflectUtils.getMethod(ServiceImpl.class, "oneToManyMethod", List.class);
        Assert.assertNotNull(oneToManyMethod);

        @SuppressWarnings("unchecked")
        InvocationHandler handler = (t, m, args) -> {
            switch (m.getName()) {
                case "noneMappedMethod":
                    return serviceImpl.noneMappedMethod(String.valueOf(args[0]));
                case "noneResultMethod":
                    serviceImpl.noneResultMethod();
                    return null;
                case "mappedMethod":
                    return serviceImpl.mappedMethod((List<String>)args[0]);
                case "onoToOneMethod":
                    return serviceImpl.onoToOneMethod((List<String>)args[0]);
                case "oneToManyMethod":
                    return serviceImpl.oneToManyMethod((List<String>)args[0]);
            }
            return m.invoke(t, args);
        };
        proxy = (Service)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Service.class}, handler);
    }

    @Test
    public void getSort() {
        Assert.assertEquals(MethodContainerFactory.DEFAULT_METHOD_CONTAINER_FACTORY_ORDER, factory.getSort());
    }

    @Test
    public void support() {
        Assert.assertTrue(factory.support(serviceImpl, noneMappedMethod, findAnnotations(noneMappedMethod)));
        Assert.assertFalse(factory.support(serviceImpl, noneResultMethod, findAnnotations(noneResultMethod)));
        Assert.assertTrue(factory.support(serviceImpl, mappedMethod, findAnnotations(mappedMethod)));
        Assert.assertTrue(factory.support(serviceImpl, onoToOneMethod, findAnnotations(onoToOneMethod)));
        Assert.assertTrue(factory.support(serviceImpl, oneToManyMethod, findAnnotations(oneToManyMethod)));
    }

    @Test
    public void getWhenNoneMappedMethod() {
        List<Container<Object>> containers = factory.get(serviceImpl, noneMappedMethod, findAnnotations(noneMappedMethod));
        Assert.assertEquals(1, containers.size());
        Container<Object> container = containers.get(0);
        Assert.assertNotNull(container);
        Assert.assertEquals("noneMappedMethod", container.getNamespace());

        Map<Object, ?> values = container.get(Arrays.asList("1", "2"));
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("1", values.get("1"));
        Assert.assertNull(values.get("2"));
    }

    @Test
    public void getWhenMappedMethod() {
        List<Container<Object>> containers = factory.get(serviceImpl, mappedMethod, findAnnotations(mappedMethod));
        Assert.assertEquals(1, containers.size());
        Container<Object> container = containers.get(0);
        Assert.assertNotNull(container);

        Assert.assertEquals("mappedMethod", container.getNamespace());
        Map<Object, ?> data = container.get(null);
        Assert.assertEquals(foo1, data.get(foo1.id));
        Assert.assertEquals(foo2, data.get(foo2.id));
    }

    @Test
    public void getProxyWhenMappedMethod() {
        List<Container<Object>> containers = factory.get(proxy, mappedMethod, findAnnotations(mappedMethod));
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
        List<Container<Object>> containers = factory.get(serviceImpl, onoToOneMethod, findAnnotations(onoToOneMethod));
        Assert.assertEquals(1, containers.size());
        Container<Object> container = containers.get(0);
        Assert.assertNotNull(container);

        Assert.assertEquals("onoToOneMethod", container.getNamespace());
        Map<Object, ?> data = container.get(null);
        Assert.assertEquals(foo1, data.get(foo1.id));
        Assert.assertEquals(foo2, data.get(foo2.id));
    }

    @Test
    public void getProxyWhenOnoToOneMethod() {
        List<Container<Object>> containers = factory.get(proxy, onoToOneMethod, findAnnotations(onoToOneMethod));
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
        List<Container<Object>> containers = factory.get(serviceImpl, oneToManyMethod, findAnnotations(oneToManyMethod));
        Assert.assertEquals(1, containers.size());
        Container<Object> container = containers.get(0);
        Assert.assertNotNull(container);

        Assert.assertEquals("oneToManyMethod", container.getNamespace());
        Map<Object, ?> data = container.get(null);
        Assert.assertEquals(Arrays.asList(foo1, foo2), data.get(foo1.name));
    }

    @Test
    public void getProxyWhenOneToManyMethod() {
        List<Container<Object>> containers = factory.get(proxy, oneToManyMethod, findAnnotations(oneToManyMethod));
        Assert.assertEquals(1, containers.size());
        Container<Object> container = containers.get(0);
        Assert.assertNotNull(container);

        Assert.assertEquals("oneToManyMethod", container.getNamespace());
        Map<Object, ?> data = container.get(null);
        Assert.assertEquals(Arrays.asList(foo1, foo2), data.get(foo1.name));
    }

    private static Collection<ContainerMethod> findAnnotations(Method method) {
        return Arrays.asList(method.getAnnotationsByType(ContainerMethod.class));
    }

    private interface Service {
        void noneResultMethod();
        Map<String, Foo> mappedMethod(List<String> args);
        Set<Foo> onoToOneMethod(List<String> args);
        List<Foo> oneToManyMethod(List<String> args);
    }

    private class ServiceImpl implements Service {

        @ContainerMethod(namespace = "noneMappedMethod", type = MappingType.NONE)
        public String noneMappedMethod(String arg) {
            return arg;
        }

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
