package cn.crane4j.springboot.util;

import cn.hutool.core.util.ArrayUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Method utils.
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodUtils {

    /**
     * Resolve method parameter names.
     *
     * @param discoverer discoverer
     * @param method method
     * @return collection of parameter name and parameter
     */
    @SuppressWarnings("all")
    public static Map<String, Parameter> resolveParameterNames(ParameterNameDiscoverer discoverer, Method method) {
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = discoverer.getParameterNames(method);
        if (ArrayUtil.isEmpty(parameters)) {
            return Collections.emptyMap();
        }
        Map<String, Parameter> parameterMap = new LinkedHashMap<>(parameters.length);
        int nameLength = ArrayUtil.length(parameterNames);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String parameterName = nameLength < i ? parameter.getName() : parameterNames[i];
            parameterMap.put(parameterName, parameter);
        }
        return parameterMap;
    }
}
