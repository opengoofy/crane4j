package cn.crane4j.core.executor;

import cn.crane4j.core.parser.*;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

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
        configuration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        parser = new TypeHierarchyBeanOperationParser(Arrays.asList(
            new AssembleAnnotationResolver(new SimpleAnnotationFinder(), configuration),
            new DisassembleAnnotationResolver(new SimpleAnnotationFinder(), configuration)
        ));
    }

    protected BeanOperations parseOperations(Class<?> type) {
        return parser.parse(type);
    }
}
