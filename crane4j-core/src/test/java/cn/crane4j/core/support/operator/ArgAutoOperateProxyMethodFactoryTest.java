package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.crane4j.core.support.auto.MethodBasedAutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.MethodBasedExpressionEvaluator;
import cn.crane4j.core.support.expression.OgnlExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionEvaluator;
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
 * test for {@link ArgAutoOperateProxyMethodFactory}
 *
 * @author huangchengxing
 */
public class ArgAutoOperateProxyMethodFactoryTest {

    @Test
    public void get() {
        // prepare
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        configuration.registerContainer(LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(Function.identity(), String::valueOf))
        ));
        BeanOperationParser operationParser = configuration.getBeanOperationsParser(BeanOperationParser.class);
        BeanOperationExecutor operationExecutor = configuration.getBeanOperationExecutor(BeanOperationExecutor.class);
        ExpressionEvaluator expressionEvaluator = new OgnlExpressionEvaluator();
        ArgAutoOperateProxyMethodFactory proxyMethodFactory = new ArgAutoOperateProxyMethodFactory(
            new MethodBasedAutoOperateAnnotatedElementResolver(configuration, configuration.getTypeResolver()),
            new MethodBasedExpressionEvaluator(SimpleParameterNameFinder.INSTANCE, expressionEvaluator, OgnlExpressionContext::new),
            SimpleParameterNameFinder.INSTANCE, SimpleAnnotationFinder.INSTANCE
        );

        // order
        Assert.assertEquals(OperatorProxyMethodFactory.ARG_AUTO_OPERATE_PROXY_METHOD_FACTORY_ORDER, proxyMethodFactory.getSort());

        // ignore none annotated method
        Method noneAnnotatedMethod = ReflectUtils.getMethod(OperatorInterface.class, "noneAnnotatedMethod", Foo.class);
        Assert.assertNotNull(noneAnnotatedMethod);
        BeanOperations beanOperations = operationParser.parse(noneAnnotatedMethod);
        Assert.assertNull(proxyMethodFactory.get(beanOperations, noneAnnotatedMethod, operationExecutor));

        // ignore none arg method
        Method noneArgMethod = ReflectUtils.getMethod(OperatorInterface.class, "noneArgMethod");
        Assert.assertNotNull(noneArgMethod);
        beanOperations = operationParser.parse(noneArgMethod);
        Assert.assertNull(proxyMethodFactory.get(beanOperations, noneArgMethod, operationExecutor));

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
        void noneAnnotatedMethod(@AutoOperate(type = Foo.class) Foo foo);
        @ArgAutoOperate
        void noneArgMethod();
        @ArgAutoOperate
        void operateMethod(@AutoOperate(type = Foo.class) Foo foo);
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
