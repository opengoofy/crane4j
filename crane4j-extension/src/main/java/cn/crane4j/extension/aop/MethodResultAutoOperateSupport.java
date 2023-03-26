package cn.crane4j.extension.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.expression.MethodBaseExpressionEvaluatorDelegate;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * Method result auto operate support.
 *
 * @author huangchengxing
 */
@Slf4j
public class MethodResultAutoOperateSupport extends MethodAnnotatedElementAutoOperateSupport {

    /**
     * method caches
     */
    protected final Map<String, ResolvedElement> methodCaches = CollectionUtils.newWeakConcurrentMap();

    /**
     * Create a {@link MethodResultAutoOperateSupport} instance
     *
     * @param configuration configuration
     * @param methodBaseExpressionEvaluatorDelegate method base expression evaluator delegate
     */
    public MethodResultAutoOperateSupport(
        Crane4jGlobalConfiguration configuration, MethodBaseExpressionEvaluatorDelegate methodBaseExpressionEvaluatorDelegate) {
        super(configuration, methodBaseExpressionEvaluatorDelegate);
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
    public void afterMethodInvoker(AutoOperate annotation, Method method, Object result, Object[] args) {
        // has annotation?
        if (Objects.isNull(annotation)) {
            return;
        }
        // whether to apply the operation?
        String condition = annotation.condition();
        if (!checkSupport(args, result, method, condition)) {
            return;
        }
        // get and build method cache
        log.debug("process result for [{}]", method.getName());
        ResolvedElement element = MapUtil.computeIfAbsent(methodCaches, method.getName(), m -> resolveElement(method, annotation));
        try {
            element.execute(result);
        } catch (Exception e) {
            log.warn("cannot process result for [{}]: [{}]", method.getName(), ExceptionUtil.getRootCause(e).getMessage());
            e.printStackTrace();
        }
    }
}
