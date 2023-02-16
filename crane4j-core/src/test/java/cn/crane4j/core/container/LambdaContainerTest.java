package cn.crane4j.core.container;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link LambdaContainer}
 *
 * @author huangchengxing
 */
public class LambdaContainerTest {

    @Test
    public void testForLambda() {
        String namespace = "lambda";
        Container<String> container = LambdaContainer.forLambda(namespace, LambdaContainerTest::getData);
        Assert.assertEquals(namespace, container.getNamespace());
        Map<String, ?> data = container.get(Arrays.asList("1", "2"));
        Assert.assertEquals("1", data.get("1"));
        Assert.assertEquals("2", data.get("2"));
    }

    private static Map<String, Object> getData(Collection<String> keys) {
        return keys.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
    }
}
