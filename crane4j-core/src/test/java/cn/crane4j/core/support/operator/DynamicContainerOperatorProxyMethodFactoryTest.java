package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.ContainerParam;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.DataProvider;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.crane4j.core.util.ReflectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link DynamicContainerOperatorProxyMethodFactory}
 *
 * @author huangchengxing
 */
public class DynamicContainerOperatorProxyMethodFactoryTest {

    private BeanOperations beanOperations;
    private BeanOperationExecutor operationExecutor;
    private DynamicContainerOperatorProxyMethodFactory proxyMethodFactory;

    @Before
    public void init() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        configuration.registerContainer(LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(Function.identity(), String::valueOf))
        ));
        BeanOperationParser operationParser = configuration.getBeanOperationsParser(BeanOperationParser.class.getSimpleName());
        beanOperations = operationParser.parse(Foo.class);
        operationExecutor = configuration.getBeanOperationExecutor(BeanOperationExecutor.class.getSimpleName());
        proxyMethodFactory = new DynamicContainerOperatorProxyMethodFactory(
            configuration.getConverterManager(), new SimpleParameterNameFinder(), new SimpleAnnotationFinder()
        );
    }

    @Test
    public void get() {
        // order
        Assert.assertEquals(DynamicContainerOperatorProxyMethodFactory.ORDER, proxyMethodFactory.getSort());

        Method oneArgMethod = ReflectUtils.getMethod(OperatorInterface.class, "oneArgMethod", Foo.class);
        MethodInvoker invoker = proxyMethodFactory.get(beanOperations, oneArgMethod, operationExecutor);
        Assert.assertNull(invoker);

        Method containerMethod = ReflectUtils.getMethod(OperatorInterface.class, "containerMethod", Foo.class, Container.class);
        invoker = proxyMethodFactory.get(beanOperations, containerMethod, operationExecutor);
        Assert.assertNotNull(invoker);

        Method mapContainerMethod = ReflectUtils.getMethod(OperatorInterface.class, "mapContainerMethod", Foo.class, HashMap.class);
        invoker = proxyMethodFactory.get(beanOperations, mapContainerMethod, operationExecutor);
        Assert.assertNotNull(invoker);

        Method dataProviderContainerMethod = ReflectUtils.getMethod(OperatorInterface.class, "dataProviderContainerMethod", Foo.class, DataProvider.class);
        invoker = proxyMethodFactory.get(beanOperations, dataProviderContainerMethod, operationExecutor);
        Assert.assertNotNull(invoker);

        Method noneAdapterMethod = ReflectUtils.getMethod(OperatorInterface.class, "noneAdapterMethod", Foo.class, Object.class);
        invoker = proxyMethodFactory.get(beanOperations, noneAdapterMethod, operationExecutor);
        Assert.assertNull(invoker);
    }

    @Test
    public void invokeWithDynamicContainer() {
        // invoke with default container
        Method containerMethod = ReflectUtils.getMethod(OperatorInterface.class, "containerMethod", Foo.class, Container.class);
        MethodInvoker invoker = proxyMethodFactory.get(beanOperations, containerMethod, operationExecutor);
        Assert.assertNotNull(invoker);
        checkFoo(1, "1", invoker, null);

        // invoke with specified container
        Container<Object> container = LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(Function.identity(), id -> "name" + id))
        );
        checkFoo(2, "name2", invoker, container);

        // do nothing when target is null
        invoker.invoke(null, null, container);
        invoker.invoke(null, null);
    }

    @Test
    public void invokeWithMapContainer() {
        Method mapContainerMethod = ReflectUtils.getMethod(OperatorInterface.class, "mapContainerMethod", Foo.class, HashMap.class);
        MethodInvoker invoker = proxyMethodFactory.get(beanOperations, mapContainerMethod, operationExecutor);
        Assert.assertNotNull(invoker);
        checkFoo(1, "1", invoker, null);

        Map<Integer, Object> map = new HashMap<>();
        map.put(2, "name2");
        checkFoo(2, "name2", invoker, map);
    }

    private <T> void checkFoo(
        Integer id, String expectName, MethodInvoker invoker, T dynamic) {
        Foo foo = new Foo(id);
        invoker.invoke(null, foo, dynamic);
        Assert.assertEquals(expectName, foo.getName());
    }

    @SuppressWarnings("unused")
    private interface OperatorInterface {
        void oneArgMethod(Foo foo);
        void containerMethod(Foo foo, @ContainerParam("test") Container<?> container);
        void mapContainerMethod(Foo foo, @ContainerParam("test") HashMap<String, Object> map);
        void dataProviderContainerMethod(Foo foo, @ContainerParam("test") DataProvider<String, Object> map);
        void noneAdapterMethod(Foo foo, Object unused);
    }

    @Setter
    @Getter
    @RequiredArgsConstructor
    private static class Foo {
        @Assemble(container = "test", props = @Mapping(ref = "name"))
        private final Integer id;
        private String name;
    }
}
