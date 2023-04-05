package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import static cn.crane4j.core.support.aop.AutoOperateMethodAnnotatedElementResolver.ResolvedElement;

/**
 * Method result auto operate support.
 *
 * @author huangchengxing
 * @see AutoOperateMethodAnnotatedElementResolver
 */
@Slf4j
public class MethodResultAutoOperateSupport {

    protected final Map<String, ResolvedElement> methodCaches = CollectionUtils.newWeakConcurrentMap();
    protected final AutoOperateMethodAnnotatedElementResolver elementResolver;
    protected final MethodBaseExpressionExecuteDelegate expressionExecuteDelegate;

    /**
     * Create a {@link MethodResultAutoOperateSupport} instance
     *
     * @param elementResolver element resolver
     * @param expressionExecuteDelegate method base expression evaluator delegate
     */
    public MethodResultAutoOperateSupport(
        AutoOperateMethodAnnotatedElementResolver elementResolver, MethodBaseExpressionExecuteDelegate expressionExecuteDelegate) {
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
        ResolvedElement element = MapUtil.computeIfAbsent(methodCaches, method.getName(), m -> elementResolver.resolve(method, annotation));
        // whether to apply the operation?
        String condition = element.getAnnotation().condition();
        if (support(method, result, args, condition)) {
            element.execute(result);
        }
    }

    private boolean support(Method method, Object result, Object[] args, String condition) {
        return CharSequenceUtil.isEmpty(condition) || Boolean.TRUE.equals(expressionExecuteDelegate.execute(condition, Boolean.class, method, args, result));
    }
}
