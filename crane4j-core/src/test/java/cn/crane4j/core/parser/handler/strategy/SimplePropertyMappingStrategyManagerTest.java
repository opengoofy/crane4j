package cn.crane4j.core.parser.handler.strategy;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

/**
 * test for {@link SimplePropertyMappingStrategyManager}
 *
 * @author huangchengxing
 */
public class SimplePropertyMappingStrategyManagerTest {

    @Test
    public void test() {
        PropertyMappingStrategyManager manager = new SimplePropertyMappingStrategyManager();
        manager.addPropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE);

        // get
        Assert.assertSame(OverwriteMappingStrategy.INSTANCE, manager.getPropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE.getName()));
        Assert.assertNull(manager.getPropertyMappingStrategy("not exist"));
        Collection<PropertyMappingStrategy> strategies = manager.getAllPropertyMappingStrategies();
        Assert.assertEquals(1, strategies.size());

        // remove
        Assert.assertSame(OverwriteMappingStrategy.INSTANCE, manager.removePropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE.getName()));
        Assert.assertNull(manager.removePropertyMappingStrategy("not exist"));
        Assert.assertNull(manager.getPropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE.getName()));
    }
}
