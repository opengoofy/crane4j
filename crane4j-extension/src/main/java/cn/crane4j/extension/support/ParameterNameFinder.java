package cn.crane4j.extension.support;

import java.lang.reflect.Method;

/**
 * Parameter name finder.
 *
 * @author huangchengxing
 */
public interface ParameterNameFinder {

    /**
     * Get parameter names
     *
     * @param method method
     * @return parameter names
     */
    String[] getParameterNames(Method method);
}
