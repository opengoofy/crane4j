package cn.crane4j.extension.spring;

import cn.crane4j.core.support.expression.MethodBasedExpressionEvaluator;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author huangchengxing
 */
public class ResolvableExpressionEvaluatorTest {

    @Test
    public void testExecute() {
        SpelExpressionEvaluator expressionEvaluator = new SpelExpressionEvaluator(new SpelExpressionParser());
        expressionEvaluator.setEmbeddedValueResolver(new EmbeddedValueResolver(new DefaultListableBeanFactory()));
        MethodBasedExpressionEvaluator evaluator = new MethodBasedExpressionEvaluator(
            new SpringParameterNameFinder(new DefaultParameterNameDiscoverer()),
            expressionEvaluator, m -> new SpelExpressionContext()
        );

        Method method = ReflectionUtils.findMethod(ResolvableExpressionEvaluatorTest.class, "compute", Integer.class, Integer.class);
        Assert.assertNotNull(method);
        String expression = "#a + #b + #result";
        MethodBasedExpressionEvaluator.MethodExecution execution = new MethodBasedExpressionEvaluator.MethodExecution(
            new Object[]{2, 3}, method, 5
        );
        Integer result = evaluator.execute(expression, Integer.class, execution);
        Assert.assertEquals((Integer)10, result);

        method = ReflectionUtils.findMethod(ResolvableExpressionEvaluatorTest.class, "compute");
        Assert.assertNotNull(method);
        expression = "#result == 0";
        execution = new MethodBasedExpressionEvaluator.MethodExecution(
            new Object[]{2, 3}, method, 0
        );
        Assert.assertEquals(Boolean.TRUE, evaluator.execute(expression, Boolean.class, execution));

        evaluator = new MethodBasedExpressionEvaluator(
            new SpringParameterNameFinder(new DefaultParameterNameDiscoverer()),
            new SpelExpressionEvaluator(new SpelExpressionParser()), m -> new SpelExpressionContext()
        );
        evaluator.execute(expression, Boolean.class, execution);
    }

    private Integer compute(Integer a, Integer b) {
        return a + b;
    }
    private Integer compute() {
        return 0;
    }
}
