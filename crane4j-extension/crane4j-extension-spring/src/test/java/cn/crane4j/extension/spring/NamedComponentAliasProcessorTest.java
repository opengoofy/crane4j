package cn.crane4j.extension.spring;

import cn.crane4j.core.support.NamedComponent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * test for {@link NamedComponentAliasProcessor}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, NamedComponentAliasProcessorTest.TestNamedComponent.class})
public class NamedComponentAliasProcessorTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        Assert.assertTrue(applicationContext.containsBean("TestNamedComponent"));
        Assert.assertTrue(applicationContext.containsBean("namedComponentAliasProcessorTest.TestNamedComponent"));
    }

    @Component
    public static class TestNamedComponent implements NamedComponent {
        @Override
        public String getName() {
            return "TestNamedComponent";
        }
    }
}
