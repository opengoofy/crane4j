package cn.crane4j.spring.boot.config.mp;

import cn.crane4j.extension.mybatis.plus.MpBaseMapperContainerRegister;
import cn.crane4j.spring.boot.config.Crane4jAutoConfiguration;
import cn.crane4j.spring.boot.config.Crane4jMybatisPlusAutoConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

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

    @Test
    public void test() {
        Map<String, MpBaseMapperContainerRegister.MapperInfo> mapperInfoMap = mapperContainerRegister.getRegisterMappers();
        Assert.assertEquals(1, mapperInfoMap.size());
        Assert.assertTrue(mapperInfoMap.containsKey("fooMapper"));
    }
}
