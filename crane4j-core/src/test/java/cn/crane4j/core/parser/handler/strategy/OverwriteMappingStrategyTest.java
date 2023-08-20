package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link OverwriteMappingStrategy}
 *
 * @author huangchengxing
 */
public class OverwriteMappingStrategyTest {

    @Test
    public void test() {
        OverwriteMappingStrategy overwriteMappingStrategy = OverwriteMappingStrategy.INSTANCE;
        PropertyMapping mapping = new SimplePropertyMapping("src", "ref");
        overwriteMappingStrategy.doMapping(
            new Object(), new Object(), new Object(), mapping, Assert::assertNotNull
        );
    }
}
