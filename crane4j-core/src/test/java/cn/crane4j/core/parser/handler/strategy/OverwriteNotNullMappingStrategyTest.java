package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link OverwriteNotNullMappingStrategy}
 *
 * @author huangchengxing
 */
public class OverwriteNotNullMappingStrategyTest {

    @Test
    public void test() {
        OverwriteNotNullMappingStrategy strategy = OverwriteNotNullMappingStrategy.INSTANCE;
        PropertyMapping mapping = new SimplePropertyMapping("src", "ref");
        strategy.doMapping(
            new Object(), new Object(), new Object(), mapping, Assert::assertNotNull
        );
        strategy.doMapping(
            new Object(), new Object(), null, mapping, t -> Assert.fail()
        );
    }
}
