package cn.crane4j.spring.boot.config.mp;

import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperationsResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.extension.mybatis.plus.MpBaseMapperContainerRegister;
import cn.crane4j.spring.boot.config.Crane4jAutoConfiguration;
import cn.crane4j.spring.boot.config.Crane4jMybatisPlusAutoConfiguration;
import cn.hutool.core.util.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Set;

/**
 * test for {@link Crane4jMybatisPlusAutoConfiguration.BaseMapperAutoRegistrar}
 *
 * @author huangchengxing
 */
@SpringBootApplication
@TestPropertySource(properties = "spring.config.location = classpath:test.yml")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jAutoConfiguration.class, Crane4jMybatisPlusAutoConfiguration.class})
public class BaseMapperAutoRegistrarTest {

    @Autowired
    private MpBaseMapperContainerRegister mapperContainerRegister;
    @Autowired
    private BeanOperationParser beanOperationParser;

    @Test
    public void test() {
        if (beanOperationParser instanceof TypeHierarchyBeanOperationParser) {
            Set<BeanOperationsResolver> resolvers = (Set<BeanOperationsResolver>)ReflectUtil.getFieldValue(beanOperationParser, "beanOperationsResolvers");
            Assert.assertEquals(2, resolvers.size());
        }

        Map<String, MpBaseMapperContainerRegister.MapperInfo> mapperInfoMap = mapperContainerRegister.getRegisterMappers();
        Assert.assertEquals(1, mapperInfoMap.size());
        Assert.assertTrue(mapperInfoMap.containsKey("fooMapper"));
    }
}
