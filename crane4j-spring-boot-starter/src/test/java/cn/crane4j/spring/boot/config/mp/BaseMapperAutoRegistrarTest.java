package cn.crane4j.spring.boot.config.mp;

import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.OperationAnnotationResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.container.AbstractQueryContainerCreator;
import cn.crane4j.extension.mybatis.plus.MybatisPlusQueryContainerRegister;
import cn.crane4j.spring.boot.config.Crane4jAutoConfiguration;
import cn.crane4j.spring.boot.config.Crane4jMybatisPlusAutoConfiguration;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
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
    private MybatisPlusQueryContainerRegister containerRegister;
    @Autowired
    private BeanOperationParser beanOperationParser;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        if (beanOperationParser instanceof TypeHierarchyBeanOperationParser) {
            Set<OperationAnnotationResolver> resolvers = (Set<OperationAnnotationResolver>)ReflectUtil.getFieldValue(beanOperationParser, "operationAnnotationResolvers");
            Assert.assertEquals(resolvers.size(), applicationContext.getBeanNamesForType(OperationAnnotationResolver.class).length);
        }

        Map<String, AbstractQueryContainerCreator.Repository<BaseMapper<?>>> mapperInfoMap = containerRegister.getRegisterRepositories();
        Assert.assertEquals(1, mapperInfoMap.size());
        Assert.assertTrue(mapperInfoMap.containsKey("fooMapper"));
    }
}
