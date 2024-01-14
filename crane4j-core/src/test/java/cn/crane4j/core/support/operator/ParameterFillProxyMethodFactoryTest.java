package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.ReflectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link ParametersFillProxyMethodFactory}
 *
 * @author huangchengxing
 */
public class ParameterFillProxyMethodFactoryTest {

    @Test
    public void get() {
        // prepare
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        configuration.registerContainer(LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(Function.identity(), String::valueOf))
        ));
        BeanOperationParser operationParser = configuration.getBeanOperationsParser(BeanOperationParser.class);
        BeanOperationExecutor operationExecutor = configuration.getBeanOperationExecutor(BeanOperationExecutor.class);
        ParametersFillProxyMethodFactory proxyMethodFactory = new ParametersFillProxyMethodFactory(operationParser);

        // order
        Assert.assertEquals(OperatorProxyMethodFactory.PARAMETERS_FILL_PROXY_METHOD_FACTORY_ORDER, proxyMethodFactory.getSort());

        // ignore none annotation method
        Method noneArgMethod = ReflectUtils.getMethod(OperatorInterface.class, "noneArgMethod");
        Assert.assertNotNull(noneArgMethod);
        BeanOperations beanOperations = operationParser.parse(noneArgMethod);
        Assert.assertNull(proxyMethodFactory.get(beanOperations, noneArgMethod, operationExecutor));

        // process annotated method
        Method operateMethod = ReflectUtils.getMethod(OperatorInterface.class, "operateMethod", Foo1.class, Foo2.class);
        Assert.assertNotNull(operateMethod);
        beanOperations = operationParser.parse(operateMethod);
        MethodInvoker invoker = proxyMethodFactory.get(beanOperations, operateMethod, operationExecutor);
        Assert.assertNotNull(invoker);

        // execute operation
        Foo1 foo1 = new Foo1(1);
        Foo1 foo2 = new Foo1(1);
        invoker.invoke(null, foo1, foo2);
        Assert.assertEquals(String.valueOf(foo1.getId()), foo1.getName());
        Assert.assertEquals(String.valueOf(foo2.getId()), foo2.getName());
    }

    private interface OperatorInterface {
        void noneArgMethod();
        void operateMethod(Foo1 foo1, Foo2 foo2);
    }

    @Setter
    @Getter
    @RequiredArgsConstructor
    private static class Foo1 {
        @Assemble(container = "test", props = @Mapping(ref = "name"))
        private final Integer id;
        private String name;
    }

    @Setter
    @Getter
    @RequiredArgsConstructor
    private static class Foo2 {
        @Assemble(container = "test", props = @Mapping(ref = "name"))
        private final Integer id;
        private String name;
    }
}
