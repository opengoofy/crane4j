package cn.crane4j.extension.spring.expression;

import cn.crane4j.core.support.expression.ExpressionContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
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
        evaluator.setEmbeddedValueResolver(new EmbeddedValueResolver(new DefaultListableBeanFactory()));
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
