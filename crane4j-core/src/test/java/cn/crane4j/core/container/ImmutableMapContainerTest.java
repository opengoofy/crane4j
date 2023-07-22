package cn.crane4j.core.container;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * test for {@link ImmutableMapContainer}
 *
 * @author tangcent
 */
public class ImmutableMapContainerTest {

    @Test
    public void forMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        Container<String> container = ImmutableMapContainer.forMap("test", map);
        assertEquals("test", container.getNamespace());
        assertEquals(map, container.get(Arrays.asList("1", "2")));
        //always return all data
        assertEquals(map, container.get(Collections.singletonList("1")));
    }

    @Test
    public void destroy() {
        // map is modifiable
        Map<String, Object> map = new HashMap<>();
        map.put("1", new Object());
        ImmutableMapContainer.forMap("test", map).destroy();
        Assert.assertTrue(map.isEmpty());

        // map is unmodifiable
        map = new HashMap<>();
        map.put("1", new Object());
        map = Collections.unmodifiableMap(map);
        ImmutableMapContainer.forMap("test", map).destroy();
        Assert.assertFalse(map.isEmpty());
    }
}