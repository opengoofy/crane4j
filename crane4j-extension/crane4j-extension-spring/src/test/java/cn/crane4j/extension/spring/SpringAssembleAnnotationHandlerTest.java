package cn.crane4j.extension.spring;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * test for {@link SpringAssembleAnnotationHandler}
 *
 * @author huangchengxing
 */
@TestPropertySource(properties = "crane4j.container-name = testContainer")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Crane4jSpringTestConfiguration.class, SpringAssembleAnnotationHandlerTest.TestConfig.class})
public class SpringAssembleAnnotationHandlerTest {

    @Autowired
    private ContainerManager containerManager;
    @Autowired
    private BeanOperationParser beanOperationParser;
    @Autowired
    private Container<String> testContainer;

    @Test
    public void parse() {
        BeanOperations beanOperations = beanOperationParser.parse(Foo.class);
        Collection<AssembleOperation> operations = beanOperations.getAssembleOperations();

        AssembleOperation operationForKey = CollectionUtils.get(operations, 0);
        Assert.assertNotNull(operationForKey);
        Assert.assertEquals("key", operationForKey.getKey());
        Assert.assertEquals(-1, operationForKey.getSort());
        Assert.assertEquals(testContainer, operationForKey.getContainer());

        AssembleOperation operationForCode = CollectionUtils.get(operations, 1);
        Assert.assertNotNull(operationForCode);
        Assert.assertEquals("code", operationForCode.getKey());
        Assert.assertEquals(0, operationForCode.getSort());
        Assert.assertEquals(testContainer, operationForCode.getContainer());

        AssembleOperation operationForId = CollectionUtils.get(operations, 2);
        Assert.assertNotNull(operationForId);
        Assert.assertEquals("id", operationForId.getKey());
        Assert.assertEquals(1, operationForId.getSort());
        Assert.assertEquals(testContainer, operationForId.getContainer());
    }

    @Configuration
    protected static class TestConfig {
        @Bean("testContainer")
        public Container<String> testContainer() {
            Map<String, Object> map = new HashMap<>();
            map.put("1", 1);
            return ConstantContainer.forMap("testContainer", map);
        }
    }

    @Data
    private static class Foo {
        @Order(1)
        @Assemble(container = "${crane4j.container-name}")
        private Integer id;
        @Order(-1)
        @Assemble(container = "@testContainer", containerProvider = "crane4jApplicationContext")
        private Integer key;
        @Assemble(container = "'test' + 'Container'", sort = 0)
        private Integer code;
    }
}
