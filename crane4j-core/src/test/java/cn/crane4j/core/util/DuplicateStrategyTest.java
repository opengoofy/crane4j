package cn.crane4j.core.util;

import cn.crane4j.annotation.DuplicateStrategy;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link DuplicateStrategy}
 *
 * @author huangchengxing
 */
public class DuplicateStrategyTest {

    @Test
    public void testAlert() {
        Assert.assertThrows(IllegalArgumentException.class, () -> DuplicateStrategy.ALERT.choose("key", "oldVal", "newVal"));
    }

    @Test
    public void testDiscardNew() {
        Assert.assertEquals("oldVal", DuplicateStrategy.DISCARD_NEW.choose("key", "oldVal", "newVal"));
    }

    @Test
    public void testDiscardOld() {
        Assert.assertEquals("newVal", DuplicateStrategy.DISCARD_OLD.choose("key", "oldVal", "newVal"));
    }

    @Test
    public void testDiscard() {
        Assert.assertNull(DuplicateStrategy.DISCARD.choose("key", "oldVal", "newVal"));
    }
}
