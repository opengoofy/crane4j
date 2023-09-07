package cn.crane4j.spring.boot.config.jackson;

import cn.crane4j.extension.jackson.JsonNodeAssistant;
import cn.crane4j.extension.jackson.JsonNodeAutoOperateModule;
import cn.crane4j.spring.boot.config.Crane4jAutoConfiguration;
import cn.crane4j.spring.boot.config.Crane4jJacksonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author huangchengxing
 */
@SpringBootApplication
@TestPropertySource(properties = "spring.config.location = classpath:test.yml")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jAutoConfiguration.class, Crane4jJacksonConfiguration.class})
public class Crane4jJacksonConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        Assert.assertNotNull(applicationContext.getBean(JsonNodeAssistant.class));
        Assert.assertNotNull(applicationContext.getBean(JsonNodeAutoOperateModule.class));
        ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
        Assert.assertNotNull(objectMapper);
        Assert.assertTrue(objectMapper.getRegisteredModuleIds().contains(JsonNodeAutoOperateModule.MODULE_NAME));
    }
}
