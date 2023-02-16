package cn.crane4j.core.exception;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * Crane's runtime exception
 *
 * @author huangchengxing
 */
public class CraneException extends RuntimeException {

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param messageTemplate the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param args args of message template
     */
    public CraneException(String messageTemplate, Object... args) {
        super(CharSequenceUtil.format(messageTemplate, args));
    }
}
