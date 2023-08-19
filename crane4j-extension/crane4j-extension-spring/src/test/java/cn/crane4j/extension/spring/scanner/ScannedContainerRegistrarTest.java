package cn.crane4j.extension.spring.scanner;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.extension.spring.DefaultCrane4jSpringConfiguration;
import cn.crane4j.extension.spring.annotation.ContainerConstantScan;
import cn.crane4j.extension.spring.annotation.ContainerEnumScan;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * test for {@link OperatorBeanDefinitionRegistrar}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, ScannedContainerRegistrarTest.Config.class})
public class ScannedContainerRegistrarTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Crane4jGlobalConfiguration configuration;

    @Test
    public void test() {
        Container<?> enumContainer = configuration.getContainer("enum");
        Assert.assertNotNull(enumContainer);
        Container<?> constantContainer = configuration.getContainer("constant");
        Assert.assertNotNull(constantContainer);
    }

    @ContainerConstantScan(includePackages = "cn.crane4j.extension.spring.scanner")
    @ContainerEnumScan(includePackages = "cn.crane4j.extension.spring.scanner")
    @Configuration
    protected static class Config {
    }
}
