package cn.createsequence.crane4j.springboot.supprot.expression;

import cn.createsequence.crane4j.core.support.expression.ExpressionContext;
import cn.createsequence.crane4j.springboot.support.expression.SpelExpressionContext;
import cn.createsequence.crane4j.springboot.support.expression.SpelExpressionEvaluator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * test for {@link SpelExpressionEvaluator}
 *
 * @author huangchengxing
 */
public class SpelExpressionEvaluatorTest {

    private SpelExpressionEvaluator evaluator;

    @Before
    public void init() {
        evaluator = new SpelExpressionEvaluator(new SpelExpressionParser());
    }

    @Test
    public void testExecute() {
        ExpressionContext context = new SpelExpressionContext();
        context.registerVariable("something", "love");
        String expected = evaluator.execute("#something + ' is necessary'", String.class, context);
        Assert.assertEquals("love is necessary", expected);
        evaluator.destroy();
    }
}
