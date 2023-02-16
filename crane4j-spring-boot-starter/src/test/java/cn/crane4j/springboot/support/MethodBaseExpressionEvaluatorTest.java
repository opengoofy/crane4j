package cn.crane4j.springboot.support;

import cn.crane4j.springboot.support.expression.SpelExpressionContext;
import cn.crane4j.springboot.support.expression.SpelExpressionEvaluator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * test for {@link MethodBaseExpressionEvaluator}
 *
 * @author huangchengxing
 */
public class MethodBaseExpressionEvaluatorTest {

    @Test
    public void testExecute() {
        MethodBaseExpressionEvaluator evaluator = new MethodBaseExpressionEvaluator(
            new DefaultParameterNameDiscoverer(),
            new SpelExpressionEvaluator(new SpelExpressionParser()),
            method -> new SpelExpressionContext()
        );

        Method method = ReflectionUtils.findMethod(MethodBaseExpressionEvaluatorTest.class, "compute", Integer.class, Integer.class);
        Assert.assertNotNull(method);
        String expression = "#a + #b + #result";
        MethodBaseExpressionEvaluator.MethodExecution execution = new MethodBaseExpressionEvaluator.MethodExecution(
            new Object[]{2, 3}, method, 5
        );
        Integer result = evaluator.execute(expression, Integer.class, execution);
        Assert.assertEquals((Integer)10, result);

        method = ReflectionUtils.findMethod(MethodBaseExpressionEvaluatorTest.class, "compute");
        Assert.assertNotNull(method);
        expression = "#result == 0";
        execution = new MethodBaseExpressionEvaluator.MethodExecution(
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
