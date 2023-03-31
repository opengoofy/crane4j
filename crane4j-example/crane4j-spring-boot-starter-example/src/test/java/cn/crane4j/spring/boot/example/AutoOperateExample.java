package cn.crane4j.spring.boot.example;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.extension.spring.Crane4jApplicationContext;
import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 示例如何使用{@link AutoOperate}以及{@link ArgAutoOperate}使用自动填充
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jSpringBootStarterExampleApplication.class, AutoOperateExample.Service.class})
public class AutoOperateExample {

    @Autowired
    private Crane4jApplicationContext context;
    @Autowired
    private Service service;

    @Before
    public void init() {
        Supplier<Container<Integer>> supplier = () -> LambdaContainer.forLambda(
            "student", ids -> ids.stream()
                .collect(Collectors.toMap(Function.identity(), id -> "student" + id))
        );
        context.replaceContainer("student", c -> ObjectUtil.defaultIfNull(c, supplier.get()));
    }

    @Test
    public void testAutoOperateArg() {
        List<StudentVO> students = service.autoOperateArg(getStudentList(5));
        checkStudentList(students);
    }

    @Test
    public void testAutoOperateResult() {
        List<StudentVO> students = service.autoOperateResult(Arrays.asList(0, 1, 2, 3, 4));
        checkStudentList(students);
    }

    @Test
    public void testAutoOperateWrappedResult() {
        Result<List<StudentVO>> result = service.autoOperateWrappedResult(Arrays.asList(0, 1, 2, 3, 4));
        checkStudentList(result.getData());
    }

    private static void checkStudentList(List<StudentVO> students) {
        for (int i = 0; i < students.size(); i++) {
            StudentVO studentVO = students.get(i);
            Assert.assertEquals("student" + i, studentVO.getName());
            System.out.println(studentVO);
        }
    }

    private static List<StudentVO> getStudentList(int range) {
        return IntStream.rangeClosed(0, range)
            .mapToObj(StudentVO::new)
            .collect(Collectors.toList());
    }

    @Component
    protected static class Service {
        /**
         * 填充入参
         */
        @ArgAutoOperate
        public List<StudentVO> autoOperateArg(
            @AutoOperate(type = StudentVO.class) List<StudentVO> students) {
            return students;
        }
        /**
         * 填充返回值
         */
        @AutoOperate(type = StudentVO.class)
        public List<StudentVO> autoOperateResult(Collection<Integer> ids) {
            return ids.stream().map(StudentVO::new).collect(Collectors.toList());
        }
        /**
         * 填充返回值，但是返回值在包装类中
         */
        @AutoOperate(type = StudentVO.class, on = "data")
        public Result<List<StudentVO>> autoOperateWrappedResult(Collection<Integer> ids) {
            return new Result<>(autoOperateResult(ids));
        }
    }

    @Data
    @RequiredArgsConstructor
    private static class Result<T> {
        private final T data;
    }

    // endregion
    @RequiredArgsConstructor
    @Data
    private static class StudentVO {
        @Assemble(container = "student", props = @Mapping(ref = "name"))
        private final Integer id;
        private String name;
    }
}
