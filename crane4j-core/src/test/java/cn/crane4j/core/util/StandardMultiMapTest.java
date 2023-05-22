package cn.crane4j.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * test for {@link StandardMultiMap}
 *
 * @author huangchengxing
 */
public class StandardMultiMapTest {

    @Test
    public void testFactoryMethod() {
        MultiMap<String, String> multiMap1 = MultiMap.arrayListMultimap();
        multiMap1.putAll("a", Arrays.asList("1", "2"));
        Assert.assertTrue(multiMap1.get("a") instanceof ArrayList);
        Assert.assertTrue(multiMap1.asMap() instanceof HashMap);

        MultiMap<String, String> multiMap2 = MultiMap.linkedHashMultimap();
        multiMap2.putAll("c", Arrays.asList("1", "2"));
        Assert.assertTrue(multiMap2.get("c") instanceof LinkedHashSet);
        Assert.assertTrue(multiMap2.asMap() instanceof LinkedHashMap);

        MultiMap<String, String> multiMap3 = MultiMap.linkedListMultimap();
        multiMap3.putAll("b", Arrays.asList("1", "2"));
        Assert.assertTrue(multiMap3.get("b") instanceof ArrayList);
        Assert.assertTrue(multiMap3.asMap() instanceof LinkedHashMap);
    }

    @Test
    public void testOperate() {
        MultiMap<String, String> multiMap = MultiMap.linkedListMultimap();
        Assert.assertTrue(multiMap.isEmpty());
        Assert.assertTrue(multiMap.put("a", "1"));
        Assert.assertTrue(multiMap.put("a", "2"));
        multiMap.putAll("b", Arrays.asList("3", "4"));

        Assert.assertFalse(multiMap.isEmpty());
        Assert.assertEquals(2, multiMap.size());
        Assert.assertEquals(2, multiMap.get("a").size());
        Assert.assertEquals(2, multiMap.get("b").size());
        Assert.assertEquals(4, multiMap.values().size());
        Assert.assertEquals(2, multiMap.keySet().size());
        Assert.assertEquals(4, multiMap.entries().size());
        Assert.assertEquals(2, multiMap.asMap().size());
        Assert.assertEquals(2, multiMap.asMap().get("a").size());
        Assert.assertEquals(2, multiMap.asMap().get("b").size());

        // after remove
        multiMap.removeAll("a");
        Assert.assertFalse(multiMap.containsKey("a"));
        Assert.assertTrue(multiMap.containsKey("b"));
        Assert.assertEquals(1, multiMap.size());
        Assert.assertEquals(0, multiMap.get("a").size());
        Assert.assertEquals(2, multiMap.get("b").size());

        // if key not exist, return empty list
        Assert.assertEquals(0, multiMap.get("c").size());

        // after clear
        multiMap.clear();
        Assert.assertTrue(multiMap.isEmpty());

        // test foreach
        multiMap.putAll("a", Arrays.asList("1", "2"));
        multiMap.putAll("b", Arrays.asList("3", "4"));
        List<String> values = new ArrayList<>();
        multiMap.forEach((k, v) -> {
            values.add(v);
        });
        Assert.assertEquals(Arrays.asList("1", "2", "3", "4"), values);

        MultiMap<String, String> multiMap2 = MultiMap.linkedHashMultimap();
        multiMap2.putAll("c", Arrays.asList("1", "2"));
        multiMap.putAll(multiMap2);
        Assert.assertEquals(3, multiMap.size());
        Assert.assertEquals(2, multiMap.get("c").size());
    }
}
