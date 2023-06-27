package cn.crane4j.extension.spring.aop;

import cn.crane4j.annotation.AutoOperate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.context.ApplicationContext;

/**
 * test for {@link AutoOperatePointcut}
 *
 * @author huangchengxing
 */
public class AutoOperatePointcutTest {

    @AutoOperate(type = Void.class)
    @Test
    public void test() {
        AutoOperatePointcut pointcut = AutoOperatePointcut.forAnnotatedMethod((method, aClass) -> true);
        Assert.assertNotNull(pointcut);

        ClassFilter filter = pointcut.getClassFilter();
        Assert.assertNotNull(filter);
        Assert.assertFalse(filter.matches(Object.class));
        Assert.assertFalse(filter.matches(ApplicationContext.class));
        Assert.assertTrue(filter.matches(AutoOperatePointcut.class));

        MethodMatcher methodMatcher = pointcut.getMethodMatcher();
        Assert.assertNotNull(methodMatcher);
        Assert.assertFalse(methodMatcher.isRuntime());
    }
}
