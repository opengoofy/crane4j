package cn.crane4j.core.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Context in parsing.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class OperationParseContext {
    @Getter
    private final BeanOperations rootOperations;
    @Getter
    private final BeanOperationParser parser;
}
