package cn.crane4j.core.exception;

/**
 * A runtime exception that may be thrown during the operation configuration resolution phase.
 *
 * @author huangchengxing
 */
public class OperationParseException extends Crane4jException {

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param messageTemplate the detail message. The detail message is saved for
     *                        later retrieval by the {@link #getMessage()} method.
     * @param args            args of message template
     */
    public OperationParseException(String messageTemplate, Object... args) {
        super(messageTemplate, args);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public OperationParseException(Throwable cause) {
        super(cause);
    }
}
