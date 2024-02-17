package cn.crane4j.core.condition;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link MultiCondition}
 *
 * @author huangchengxing
 */
public class MultiConditionTest {

    @Test
    public void test() {
        Condition alwaysTrue = (t, op) -> true;
        Condition alwaysFalse = (t, op) -> false;

        // and
        Assert.assertTrue(MultiCondition.and(alwaysTrue, alwaysTrue).test(null, null));
        Assert.assertFalse(MultiCondition.and(alwaysTrue, alwaysFalse).test(null, null));

        // or
        Assert.assertTrue(MultiCondition.or(alwaysTrue, alwaysFalse).test(null, null));
        Assert.assertFalse(MultiCondition.or(alwaysFalse, alwaysFalse).test(null, null));
    }
}
