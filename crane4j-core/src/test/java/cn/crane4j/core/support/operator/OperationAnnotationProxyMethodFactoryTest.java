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
 * test for {@link OperationAnnotationProxyMethodFactory}
 *
 * @author huangchengxing
 */
public class OperationAnnotationProxyMethodFactoryTest {

    @Test
    public void get() {
        // prepare
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        configuration.registerContainer(LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(Function.identity(), String::valueOf))
        ));
        BeanOperationParser operationParser = configuration.getBeanOperationsParser(BeanOperationParser.class);
        BeanOperationExecutor operationExecutor = configuration.getBeanOperationExecutor(BeanOperationExecutor.class);
        OperationAnnotationProxyMethodFactory proxyMethodFactory = new OperationAnnotationProxyMethodFactory(configuration.getConverterManager());

        // order
        Assert.assertEquals(OperatorProxyMethodFactory.OPERATION_ANNOTATION_PROXY_METHOD_FACTORY_ORDER, proxyMethodFactory.getSort());

        // ignore none annotation method
        Method noneAnnotationMethod = ReflectUtils.getMethod(OperatorInterface.class, "noneAnnotationMethod", Foo.class);
        Assert.assertNotNull(noneAnnotationMethod);
        BeanOperations beanOperations = operationParser.parse(noneAnnotationMethod);
        Assert.assertNull(proxyMethodFactory.get(beanOperations, noneAnnotationMethod, operationExecutor));

        // process annotated method
        Method operateMethod = ReflectUtils.getMethod(OperatorInterface.class, "operateMethod", Foo.class);
        Assert.assertNotNull(operateMethod);
        beanOperations = operationParser.parse(operateMethod);
        MethodInvoker invoker = proxyMethodFactory.get(beanOperations, operateMethod, operationExecutor);
        Assert.assertNotNull(invoker);

        // execute operation
        Foo foo = new Foo(1);
        invoker.invoke(null, foo);
        Assert.assertEquals(String.valueOf(foo.getId()), foo.getName());
    }

    private interface OperatorInterface {
        void noneAnnotationMethod(Foo foo);
        @Assemble(container = "test", key = "id", props = @Mapping(ref = "name"))
        void operateMethod(Foo foo);
    }

    @Setter
    @Getter
    @RequiredArgsConstructor
    private static class Foo {
        private final Integer id;
        private String name;
    }
}
