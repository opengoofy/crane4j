package cn.crane4j.core.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link SimplePropertyMapping}
 *
 * @author huangchengxing
 */
public class SimplePropertyMappingTest {

    private SimplePropertyMapping mapping;

    @Before
    public void init() {
        mapping = new SimplePropertyMapping("name", "userName");
    }

    @Test
    public void getSource() {
        Assert.assertEquals("name", mapping.getSource());
    }

    @Test
    public void hasSource() {
        Assert.assertTrue(mapping.hasSource());
    }

    @Test
    public void getReference() {
        Assert.assertEquals("userName", mapping.getReference());
    }
}
