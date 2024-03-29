package cn.crane4j.core.executor;

import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.ConditionalTypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author huangchengxing
 */
public class BaseExecutorTest {

    protected SimpleCrane4jGlobalConfiguration configuration;
    private BeanOperationParser parser;

    @Test
    public void checkParserInitialized() {
        Assert.assertNotNull(parser);
    }

    @Before
    public void initParser() {
        configuration = SimpleCrane4jGlobalConfiguration.create();
        parser = configuration.getBeanOperationsParser(null, ConditionalTypeHierarchyBeanOperationParser.class);
    }

    protected BeanOperations parseOperations(Class<?> type) {
        return parser.parse(type);
    }
}
