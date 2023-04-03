package cn.crane4j.core.executor;

import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.DefaultAnnotationOperationsResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import org.junit.Before;

import java.util.Collections;

/**
 * @author huangchengxing
 */
public class BaseExecutorTest {

    protected SimpleCrane4jGlobalConfiguration configuration;
    private BeanOperationParser parser;

    @Before
    public void initParser() {
        configuration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        parser = new TypeHierarchyBeanOperationParser(Collections.singletonList(
            new DefaultAnnotationOperationsResolver(new SimpleAnnotationFinder(), configuration)
        ));
    }

    protected BeanOperations parseOperations(Class<?> type) {
        return parser.parse(type);
    }
}
