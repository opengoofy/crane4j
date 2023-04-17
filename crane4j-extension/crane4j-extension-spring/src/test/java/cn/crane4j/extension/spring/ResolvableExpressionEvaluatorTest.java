package cn.crane4j.extension.spring;

import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * test for {@link ResolvableExpressionEvaluator}
 *
 * @author huangchengxing
 */
public class ResolvableExpressionEvaluatorTest {

    @Test
    public void testExecute() {
        ResolvableExpressionEvaluator evaluator = new ResolvableExpressionEvaluator(
            new SpringParameterNameFinder(new DefaultParameterNameDiscoverer()),
            new SpelExpressionEvaluator(new SpelExpressionParser()),
            method -> new SpelExpressionContext()
        );

        Method method = ReflectionUtils.findMethod(ResolvableExpressionEvaluatorTest.class, "compute", Integer.class, Integer.class);
        Assert.assertNotNull(method);
        String expression = "#a + #b + #result";
        ResolvableExpressionEvaluator.MethodExecution execution = new ResolvableExpressionEvaluator.MethodExecution(
            new Object[]{2, 3}, method, 5
        );
        Integer result = evaluator.execute(expression, Integer.class, execution);
        Assert.assertEquals((Integer)10, result);

        method = ReflectionUtils.findMethod(ResolvableExpressionEvaluatorTest.class, "compute");
        Assert.assertNotNull(method);
        expression = "#result == 0";
        execution = new ResolvableExpressionEvaluator.MethodExecution(
            new Object[]{2, 3}, method, 0
        );
        Assert.assertTrue(evaluator.execute(expression, Boolean.class, execution));
    }

    private Integer compute(Integer a, Integer b) {
        return a + b;
    }
    private Integer compute() {
        return 0;
    }
}
