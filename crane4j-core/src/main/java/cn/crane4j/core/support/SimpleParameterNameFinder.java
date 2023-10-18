package cn.crane4j.core.support;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Simple implementation of {@link ParameterNameFinder}.
 *
 * @author huangchengxing
 * @see Parameter#getName()
 */
public class SimpleParameterNameFinder implements ParameterNameFinder {

    public static final SimpleParameterNameFinder INSTANCE = new SimpleParameterNameFinder();

    /**
     * empty array
     */
    private static final String[] EMPTY_ARRAY = new String[0];

    /**
     * Get parameter names
     *
     * @param method method
     * @return parameter names
     */
    @Override
    public String[] getParameterNames(Method method) {
        return Objects.isNull(method) ?
            EMPTY_ARRAY : Stream.of(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
    }
}
