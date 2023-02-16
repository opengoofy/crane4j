package cn.crane4j.springboot.support;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.KeyTriggerOperation;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.springboot.annotation.AutoOperate;
import cn.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于方法与方法参数的上的{@link AutoOperate}注解进行自动操作的公共模板类
 *
 * @author huangchengxing
 * @see AutoOperate
 * @see MethodBaseExpressionEvaluator
 * @see MethodArgumentAutoOperateAspect
 * @see MethodResultAutoOperateAspect
 */
@RequiredArgsConstructor
public class MethodAnnotatedElementAutoOperateSupport {

    private final ApplicationContext applicationContext;
    private final MethodBaseExpressionEvaluator methodBaseExpressionEvaluator;

    /**
     * 根据表达式计算结果，检查是否要应用该操作
     *
     * @param args 方法参数
     * @param result 方法返回值，可能为{@code null}
     * @param method 当前执行的方法
     * @param condition 应用条件表达式
     * @return 是否要应用该操作
     */
    public boolean checkSupport(Object[] args, Object result, Method method, String condition) {
        if (CharSequenceUtil.isEmpty(condition)) {
            return true;
        }
        MethodBaseExpressionEvaluator.MethodExecution methodContext = new MethodBaseExpressionEvaluator.MethodExecution(args, method, result);
        Boolean support = methodBaseExpressionEvaluator.execute(condition, Boolean.class, methodContext);
        return Objects.equals(Boolean.TRUE, support);
    }

    /**
     * 解析元素上的{@link AutoOperate}注解，根据其配置为其构建{@link ResolvedElement}
     *
     * @param element 被注解的元素
     * @param annotation 注解
     * @return {@link ResolvedElement}
     */
    public ResolvedElement resolveElement(AnnotatedElement element, AutoOperate annotation) {
        // 获取待执行对象
        MethodInvoker extractor = resolveExtractor(element, annotation);
        // 根据注解配置获取相关组件
        BeanOperationParser parser = applicationContext.getBean(annotation.parser());
        BeanOperations beanOperations = parser.parse(annotation.type());
        BeanOperationExecutor executor = applicationContext.getBean(annotation.executor());
        // 检查组别
        Set<String> groups = resolveGroups(annotation);
        return new ResolvedElement(element, extractor, groups, beanOperations, executor);
    }

    private MethodInvoker resolveExtractor(AnnotatedElement element, AutoOperate annotation) {
        Class<?> type = resolveTypeForExtractor(element);
        String on = annotation.on();
        MethodInvoker extractor = (t, args) -> t;
        if (CharSequenceUtil.isNotEmpty(on)) {
            PropertyOperator propertyOperator = applicationContext.getBean(PropertyOperator.class);
            extractor = propertyOperator.findGetter(type, on);
            Objects.requireNonNull(extractor, () -> CharSequenceUtil.format(
                "cannot find getter for [{}] on [{}]", on, annotation.type()
            ));
        }
        return extractor;
    }

    private static Class<?> resolveTypeForExtractor(AnnotatedElement element) {
        if (element instanceof Method) {
            return ((Method)element).getReturnType();
        } else if (element instanceof Parameter) {
            return ((Parameter)element).getType();
        } else {
            throw new IllegalArgumentException("element must be a method or parameter");
        }
    }

    protected static Set<String> resolveGroups(AutoOperate annotation) {
        String[] includes = annotation.includes();
        String[] excludes = annotation.excludes();
        return Stream.of(includes)
            .filter(in -> !ArrayUtil.contains(excludes, in))
            .collect(Collectors.toSet());
    }

    /**
     * 被解析的注解元素
     */
    @Getter
    @RequiredArgsConstructor
    public static class ResolvedElement {
        private final AnnotatedElement element;
        private final MethodInvoker extractor;
        private final Set<String> groups;
        private final BeanOperations beanOperations;
        private final BeanOperationExecutor executor;
        public void execute(Object data) {
            Object target = extractor.invoke(data);
            if (Objects.nonNull(target)) {
                Predicate<? super KeyTriggerOperation> filter = groups.isEmpty() ?
                    op -> true : op -> groups.contains(op.getKey());
                executor.execute(CollectionUtils.adaptObjectToCollection(target), beanOperations, filter);
            }
        }
    }
}
