package cn.createsequence.crane4j.springboot;

import cn.hutool.core.util.ArrayUtil;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author huangchengxing
 */
public class MethodUtils {

    /**
     * 解析方法参数名
     *
     * @param discoverer 参数名查找器
     * @param method 方法
     * @return 参数名-参数对象集合
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
