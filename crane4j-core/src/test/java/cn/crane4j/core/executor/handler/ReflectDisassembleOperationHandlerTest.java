package cn.crane4j.core.executor.handler;

import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BaseExecutorTest;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.DisassembleOperation;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * test for {@link ReflectDisassembleOperationHandler}
 *
 * @author huangchengxing
 */
public class ReflectDisassembleOperationHandlerTest extends BaseExecutorTest {

    private ReflectDisassembleOperationHandler handler;

    @Before
    public void init() {
        handler = new ReflectDisassembleOperationHandler(new ReflectPropertyOperator());
    }

    @Test
    public void process() {
        BeanOperations beanOperations = parseOperations(Bean.class);
        Collection<DisassembleOperation> operations = beanOperations.getDisassembleOperations();

        Bean root = new Bean();
        DisassembleOperation operationForBean = CollectionUtils.get(operations, 0);
        Assert.assertEquals("bean", operationForBean.getKey());
        root.setBean(new Bean());
        checkDisassembledBeans(operationForBean, root, 1);

        DisassembleOperation operationForBeanArray = CollectionUtils.get(operations, 1);
        Assert.assertEquals("beanArray", operationForBeanArray.getKey());
        root.setBeanArray(new Bean[]{new Bean(), new Bean(), new Bean()});
        checkDisassembledBeans(operationForBeanArray, root, 3);

        DisassembleOperation operationForBeanList = CollectionUtils.get(operations, 2);
        Assert.assertEquals("beanList", operationForBeanList.getKey());
        root.setBeanList(Arrays.asList(new Bean(), new Bean(), new Bean()));
        checkDisassembledBeans(operationForBeanList, root, 3);

        DisassembleOperation operationForBeanMultiList = CollectionUtils.get(operations, 3);
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
        Assert.assertTrue(handler.process(operation, null).isEmpty());

        // 没有getter方法
        Runnable runnable = () -> handler.process(operation, Collections.singleton(new Bean()));
        Assert.assertThrows(Crane4jException.class, runnable::run);
    }

    @Accessors(chain = true)
    @Setter
    private static class Bean {
        @Getter
        @Disassemble(type = Bean.class)
        private Bean bean;
        @Getter
        @Disassemble(type = Bean.class)
        private Bean[] beanArray;
        @Getter
        @Disassemble(type = Bean.class)
        private List<Bean> beanList;
        @Getter
        @Disassemble(type = Bean.class)
        private List<List<Bean[]>> beanMultiList;
        @Disassemble(type = Bean.class)
        private Bean noneGetter;
    }
}
