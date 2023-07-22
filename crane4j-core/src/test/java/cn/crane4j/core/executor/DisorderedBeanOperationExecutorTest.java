package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.parser.BeanOperations;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * test for {@link DisorderedBeanOperationExecutor}
 *
 * @author huangchengxing
 */
public class DisorderedBeanOperationExecutorTest extends BaseExecutorTest {

    private DisorderedBeanOperationExecutor executor;

    @Before
    public void init() {
        executor = new DisorderedBeanOperationExecutor(configuration);

        Map<Integer, Object> sources = new HashMap<>();
        sources.put(1, new Source(1, "one"));
        sources.put(2, new Source(2, "two"));
        Container<Integer> container = Containers.forMap("test", sources);
        configuration.registerContainer(container);
    }

    @SuppressWarnings("all")
    @Test
    public void execute() {
        Bean bean1 = new Bean().setId(1).setNestedBean(new NestedBean().setType(2));
        Bean bean2 = new Bean().setId(2).setNestedBean(new NestedBean().setType(1));
        List<Bean> beanList = Arrays.asList(bean1, bean2);

        BeanOperations beanOperations = parseOperations(Bean.class);
        executor.execute(beanList, beanOperations);

        Assert.assertEquals("one", bean1.getName());
        Assert.assertEquals("two", bean1.getNestedBean().getTypeName());
        Assert.assertEquals("two", bean2.getName());
        Assert.assertEquals("one", bean2.getNestedBean().getTypeName());
    }

    @Getter
    @RequiredArgsConstructor
    private static class Source {
        private final Integer key;
        private final String value;
    }

    @Accessors(chain = true)
    @Data
    private static class Bean {
        @Assemble(container = "test", props = @Mapping(ref = "name", src = "value"))
        private Integer id;
        private String name;
        @Disassemble(type = NestedBean.class)
        private NestedBean nestedBean;
    }

    @Accessors(chain = true)
    @Data
    private static class NestedBean {
        @Assemble(container = "test", props = @Mapping(ref = "typeName", src = "value"))
        private Integer type;
        private String typeName;
    }

}
