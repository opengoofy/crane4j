package cn.crane4j.spring.boot.example;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.support.OperateTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 演示如何使用基于方法的数据源容器：
 * 1.通过{@link ContainerMethod}将{@link Service#queryStudents}方法声明为数据源；
 * 2.启动spring，自动识别注册该方法数据源；
 * 3.通过{@link Assemble#container()}注解配置以该容器进行装撇；
 * 4.执行操作，完成填充；
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jSpringBootStarterExampleApplication.class, ContainerMethodExample.Service.class})
public class ContainerMethodExample {

    @Autowired
    private OperateTemplate operateTemplate;
    @Autowired
    private Service service;

    @Test
    public void test() {
        Assert.assertNotNull(service);
        List<StudentVO> students = IntStream.rangeClosed(0, 4)
            .mapToObj(StudentVO::new)
            .collect(Collectors.toList());
        operateTemplate.execute(students, StudentVO.class);
        for (int i = 0; i < students.size(); i++) {
            StudentVO studentVO = students.get(i);
            Assert.assertEquals("student" + i, studentVO.getName());
            Assert.assertEquals((Integer)i, studentVO.getOriginId());
            System.out.println(studentVO);
        }
    }

    @Component
    protected static class Service {
        @ContainerMethod(namespace = "student", resultType = Student.class)
        public List<Student> queryStudents(Collection<Integer> ids) {
            return ids.stream()
                .map(id -> new Student(id, "student" + id))
                .collect(Collectors.toList());
        }

        @AutoOperate(type = StudentVO.class)
        public List<StudentVO> getStudents(Collection<Integer> ids) {
            return ids.stream()
                .map(StudentVO::new)
                .collect(Collectors.toList());
        }
    }

    @RequiredArgsConstructor
    @Data
    private static class StudentVO {
        @Assemble(container = "student", props = {
            @Mapping(src = "name", ref = "name"), @Mapping(src = "id", ref = "originId")
        })
        private final Integer id;
        private Integer originId;
        private String name;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class Student {
        private Integer id;
        private String name;
    }
}
