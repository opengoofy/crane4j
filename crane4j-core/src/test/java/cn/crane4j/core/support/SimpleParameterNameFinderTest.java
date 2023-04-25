package cn.crane4j.core.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link SimpleParameterNameFinder}
 *
 * @author huangchengxing
 */
public class SimpleParameterNameFinderTest {

    @Test
    public void getParameterNames() throws NoSuchMethodException {
        SimpleParameterNameFinder simpleParameterNameFinder = new SimpleParameterNameFinder();
        Assert.assertEquals(0, simpleParameterNameFinder.getParameterNames(null).length);
        String[] parameterNames = simpleParameterNameFinder.getParameterNames(
            SimpleParameterNameFinderTest.class.getDeclaredMethod("test", String.class, Integer.class)
        );
        Assert.assertArrayEquals(new String[]{ "arg0", "arg1" }, parameterNames);
    }

    private static void test(String p1, Integer p2) {
        System.out.println("test");
    }
}
