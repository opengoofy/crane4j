package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.container.Container;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * test for {@link ContainerInstanceLifecycleProcessor}
 *
 * @author huangchengxing
 */
public class ContainerInstanceLifecycleProcessorTest {

    @Test
    public void test() {
        ContainerInstanceLifecycleProcessor processor = new ContainerInstanceLifecycleProcessor();
        TestContainer container = new TestContainer();
        processor.whenCreated(null, container);
        Assert.assertEquals(1, container.getCount().intValue());
        processor.whenDestroyed(container);
        Assert.assertEquals(0, container.getCount().intValue());
    }

    @Getter
    private static class TestContainer implements Container<Object>, Container.Lifecycle {
        private final String namespace = "test";
        private Integer count = 0;
        @Override
        public Map<Object, ?> get(Collection<Object> keys) {
            return Collections.emptyMap();
        }
        @Override
        public void init() {
            this.count++;
        }
        @Override
        public void destroy() {
            this.count--;
        }
    }
}
