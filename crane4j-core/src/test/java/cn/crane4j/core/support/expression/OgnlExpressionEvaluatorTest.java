package cn.crane4j.core.support.expression;

import cn.crane4j.core.exception.Crane4jException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link OgnlExpressionEvaluator}
 *
 * @author huangchengxing
 */
public class OgnlExpressionEvaluatorTest {

    private OgnlExpressionEvaluator evaluator;

    @Before
    public void init() {
        evaluator = new OgnlExpressionEvaluator();
    }

    @Test
    public void testExecute() {
        ExpressionContext context = new OgnlExpressionContext();
        context.registerVariable("something", "love");
        Assert.assertThrows(Crane4jException.class, () -> evaluator.execute("something + is necessary", String.class, context));
        String expected = evaluator.execute("#something + ' is necessary'", String.class, context);
        Assert.assertEquals("love is necessary", expected);
    }
}
