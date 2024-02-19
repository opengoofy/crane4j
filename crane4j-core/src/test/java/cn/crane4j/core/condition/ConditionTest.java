package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionType;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link Condition}
 *
 * @author huangchengxing
 */
public class ConditionTest {

    @Test
    public void test() {
        Condition condition = (t, op) -> true;
        Assert.assertTrue(condition.test(null, null));

        Condition negation = condition.negate();
        Assert.assertFalse(negation.test(null, null));

        Condition and = condition.and(condition);
        Assert.assertTrue(and.test(null, null));

        Condition or = condition.or(negation);
        Assert.assertTrue(or.test(null, null));

        Assert.assertSame(ConditionType.AND, condition.getType());
    }
}
