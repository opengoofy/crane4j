package cn.crane4j.core.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Context in parsing.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class OperationParseContext {
    @Getter
    private final BeanOperations rootOperations;
    private final Map<Class<?>, BeanOperations> resolvedTypes;
    @Getter
    private final BeanOperationParser parser;
}
