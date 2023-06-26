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
 * test for {@link DefaultOperatorProxyMethodFactory}
 *
 * @author huangchengxing
 */
public class DefaultOperatorProxyMethodFactoryTest {

    @Test
    public void get() {
        // prepare
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        configuration.registerContainer(LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(Function.identity(), String::valueOf))
        ));
        BeanOperationParser operationParser = configuration.getBeanOperationsParser(BeanOperationParser.class.getSimpleName());
        BeanOperations beanOperations = operationParser.parse(Foo.class);
        BeanOperationExecutor operationExecutor = configuration.getBeanOperationExecutor(BeanOperationExecutor.class.getSimpleName());
        DefaultOperatorProxyMethodFactory proxyMethodFactory = new DefaultOperatorProxyMethodFactory(configuration.getConverterManager());

        // order
        Assert.assertEquals(DefaultOperatorProxyMethodFactory.ORDER, proxyMethodFactory.getSort());

        // generate proxy method
        Method noneArgMethod = ReflectUtils.getMethod(OperatorInterface.class, "oneArgMethod", Foo.class);
        Assert.assertNotNull(noneArgMethod);
        MethodInvoker invoker = proxyMethodFactory.get(beanOperations, noneArgMethod, operationExecutor);
        Assert.assertNotNull(invoker);

        // execute operation
        Foo foo = new Foo(1);
        invoker.invoke(null, foo);
        Assert.assertEquals(String.valueOf(foo.getId()), foo.getName());
    }

    private interface OperatorInterface {
        void oneArgMethod(Foo foo);
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
