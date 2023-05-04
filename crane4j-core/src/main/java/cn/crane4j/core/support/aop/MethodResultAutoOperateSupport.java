package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * <p>Support class for completing the operation for result of method which annotated by {@link AutoOperate}.
 *
 * <p>Before the method is called, the method return type will be resolved
 * to {@link AutoOperateAnnotatedElement} by {@link AutoOperateAnnotatedElementResolver} and cached.<br />
 * After the method is called, the {@link AutoOperateAnnotatedElement}
 * will be used to complete the operation of data from the method result.
 *
 * <p>Support expression for {@link AutoOperate#condition()}, if the expression is not empty,
 * the expression will be evaluated by {@link MethodBaseExpressionExecuteDelegate},
 * only when the expression returns true or "true", the operation will be applied.
 *
 * @author huangchengxing
 * @see AutoOperateAnnotatedElementResolver
 */
@Slf4j
public class MethodResultAutoOperateSupport {

    protected final Map<String, AutoOperateAnnotatedElement> methodCaches = CollectionUtils.newWeakConcurrentMap();
    protected final AutoOperateAnnotatedElementResolver elementResolver;
    protected final MethodBaseExpressionExecuteDelegate expressionExecuteDelegate;

    /**
     * Create a {@link MethodResultAutoOperateSupport} instance
     *
     * @param elementResolver element resolver
     * @param expressionExecuteDelegate method base expression evaluator delegate
     */
    public MethodResultAutoOperateSupport(
        AutoOperateAnnotatedElementResolver elementResolver, MethodBaseExpressionExecuteDelegate expressionExecuteDelegate) {
        this.elementResolver = elementResolver;
        this.expressionExecuteDelegate = expressionExecuteDelegate;
    }

    /**
     * After the method is called, process the returning result
     * of the method according to the configuration of {@link ArgAutoOperate} annotation.
     *
     * @param annotation annotation
     * @param method method
     * @param result result
     * @param args args
     */
    public void afterMethodInvoke(AutoOperate annotation, Method method, Object result, Object[] args) {
        // has annotation?
        if (Objects.isNull(annotation)) {
            return;
        }
        // get and build method cache
        log.debug("process result for [{}]", method.getName());
        AutoOperateAnnotatedElement element = CollectionUtils.computeIfAbsent(methodCaches, method.getName(), m -> elementResolver.resolve(method, annotation));
        // whether to apply the operation?
        String condition = element.getAnnotation().condition();
        if (support(method, result, args, condition)) {
            element.execute(result);
        }
    }

    private boolean support(Method method, Object result, Object[] args, String condition) {
        return StringUtils.isEmpty(condition) || Boolean.TRUE.equals(expressionExecuteDelegate.execute(condition, Boolean.class, method, args, result));
    }
}
