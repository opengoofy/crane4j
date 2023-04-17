package cn.crane4j.extension.spring;

import cn.crane4j.core.support.ParameterNameFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;

/**
 * A {@link ParameterNameFinder} implementation based on Spring's {@link ParameterNameDiscoverer}.
 *
 * @author huangchengxing
 * @see ParameterNameDiscoverer
 */
@RequiredArgsConstructor
public class SpringParameterNameFinder implements ParameterNameFinder {

    private final ParameterNameDiscoverer parameterNameDiscoverer;

    @Override
    public String[] getParameterNames(Method method) {
        return parameterNameDiscoverer.getParameterNames(method);
    }
}
