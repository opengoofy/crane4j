package cn.crane4j.core.executor.handler;

import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.executor.BaseExecutorTest;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * test for {@link ReflectiveDisassembleOperationHandler}
 *
 * @author huangchengxing
 */
public class ReflectiveDisassembleOperationHandlerTest extends BaseExecutorTest {

    private ReflectiveDisassembleOperationHandler handler;

    @Before
    public void init() {
        handler = new ReflectiveDisassembleOperationHandler(new ReflectivePropertyOperator(new HutoolConverterManager()));
    }

    @Test
    public void process() {
        BeanOperations beanOperations = parseOperations(Bean.class);
        Collection<DisassembleOperation> operations = beanOperations.getDisassembleOperations();
        List<DisassembleOperation> sortedOperations = new ArrayList<>(operations);
        sortedOperations.sort(Comparator.comparing(DisassembleOperation::getKey));

        Bean root = new Bean();
        DisassembleOperation operationForBean = CollectionUtils.get(sortedOperations, 0);
        Assert.assertEquals("bean", operationForBean.getKey());
        root.setBean(new Bean());
        checkDisassembledBeans(operationForBean, root, 1);

        DisassembleOperation operationForBeanArray = CollectionUtils.get(sortedOperations, 1);
        Assert.assertEquals("beanArray", operationForBeanArray.getKey());
        root.setBeanArray(new Bean[]{new Bean(), new Bean(), new Bean()});
        checkDisassembledBeans(operationForBeanArray, root, 3);

        DisassembleOperation operationForBeanList = CollectionUtils.get(sortedOperations, 2);
        Assert.assertEquals("beanList", operationForBeanList.getKey());
        root.setBeanList(Arrays.asList(new Bean(), new Bean(), new Bean()));
        checkDisassembledBeans(operationForBeanList, root, 3);

        DisassembleOperation operationForBeanMultiList = CollectionUtils.get(sortedOperations, 3);
        Assert.assertEquals("beanMultiList", operationForBeanMultiList.getKey());
        root.setBeanMultiList(Arrays.asList(
            Arrays.asList(new Bean[]{new Bean(), new Bean()}, new Bean[]{new Bean(), new Bean()}),
            Arrays.asList(new Bean[]{new Bean(), new Bean()}, new Bean[]{new Bean(), new Bean()})
        ));
        checkDisassembledBeans(operationForBeanMultiList, root, 8);
    }

    private void checkDisassembledBeans(DisassembleOperation operation, Object object, int expectedSize) {
        Collection<?> disassembledBeans = handler.process(operation, Collections.singleton(object));
        Assert.assertEquals(expectedSize, disassembledBeans.size());
    }

    @Test
    public void processWhenExceptionalCase() {
        BeanOperations beanOperations = parseOperations(Bean.class);
        Collection<DisassembleOperation> operations = beanOperations.getDisassembleOperations();

        DisassembleOperation operation = CollectionUtils.get(operations, 4);
        Assert.assertEquals("noneGetter", operation.getKey());

        // 输入null
        Assert.assertTrue(handler.process(operation, null)
            .isEmpty());

        // 没有getter方法
        Bean bean = new Bean();
        bean.noneGetter = new Bean();
        Assert.assertEquals(1,handler.process(operation, Collections.singleton(bean)).size());
    }

    @Accessors(chain = true)
    @Setter
    private static class Bean {
        @Getter
        @Disassemble(type = Bean.class, sort = 1)
        private Bean bean;
        @Getter
        @Disassemble(type = Bean.class, sort = 2)
        private Bean[] beanArray;
        @Getter
        @Disassemble(type = Bean.class, sort = 4)
        private List<Bean> beanList;
        @Getter
        @Disassemble(type = Bean.class, sort = 4)
        private List<List<Bean[]>> beanMultiList;
        @Disassemble(type = Bean.class, sort = 5)
        private Bean noneGetter;
    }
}
