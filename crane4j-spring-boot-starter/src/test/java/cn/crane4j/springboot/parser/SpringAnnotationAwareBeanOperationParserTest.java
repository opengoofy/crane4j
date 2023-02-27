package cn.crane4j.springboot.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.springboot.config.Crane4jAutoConfiguration;
import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * test for {@link SpringAnnotationAwareBeanOperationParser}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jAutoConfiguration.class, SpringAnnotationAwareBeanOperationParserTest.TestConfig.class})
public class SpringAnnotationAwareBeanOperationParserTest {

    @Autowired
    private BeanOperationParser beanOperationParser;
    @Autowired
    private Container<String> testContainer;

    @Test
    public void parse() {
        BeanOperations beanOperations = beanOperationParser.parse(Foo.class);
        Collection<AssembleOperation> operations = beanOperations.getAssembleOperations();

        AssembleOperation operationForKey = CollUtil.get(operations, 0);
        Assert.assertEquals("key", operationForKey.getKey());
        Assert.assertEquals(-1, operationForKey.getSort());
        Assert.assertEquals(testContainer, operationForKey.getContainer());

        AssembleOperation operationForCode = CollUtil.get(operations, 1);
        Assert.assertEquals("code", operationForCode.getKey());
        Assert.assertEquals(0, operationForCode.getSort());
        Assert.assertEquals(testContainer, operationForCode.getContainer());

        AssembleOperation operationForId = CollUtil.get(operations, 2);
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
            return ConstantContainer.forMap("test", map);
        }
    }

    @Data
    private static class Foo {
        @Order(1)
        @Assemble(container = "test")
        private Integer id;
        @Order(-1)
        @Assemble(container = "@testContainer", containerProviderName = "crane4jApplicationContext")
        private Integer key;
        @Assemble(container = "'test' + 'Container'", sort = 0)
        private Integer code;
    }
}
