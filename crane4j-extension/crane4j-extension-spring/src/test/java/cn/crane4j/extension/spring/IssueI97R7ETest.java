package cn.crane4j.extension.spring;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.support.OperateTemplate;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, IssueI97R7ETest.Server.class})
public class IssueI97R7ETest {

    @Autowired
    private OperateTemplate operateTemplate;

    @Test
    public void test() {
        Foo foo = new Foo(1L);
        operateTemplate.execute(foo);
        System.out.println(foo);
    }

    @Assemble(
        container = "USER_ROLE_ID_LIST",
        key = "id",
        props = @Mapping(ref = "roleIds")
    )
    @Data
    @RequiredArgsConstructor
    public static class Foo {
        private final Long id;
        private List<Long> roleIds;
    }

    @SuppressWarnings("unused")
    @Component
    public static class Server {
        @ContainerMethod(namespace = "USER_ROLE_ID_LIST", type = MappingType.ORDER_OF_KEYS)
        public List<Long> listRoleIdByUserId(Long userId){
            return Arrays.asList(userId + 1, userId + 2);
        }
    }
}
