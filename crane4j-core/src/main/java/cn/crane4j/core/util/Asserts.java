package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p>Asserts tool class.
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Asserts {

    /**
     * <p>Asserts that the obj1 is equals obj2, otherwise throw an exception.
     *
     * @param obj1 obj1
     * @param obj2 obj2
     * @param ex exception supplier
     */
    public static void isEquals(Object obj1, Object obj2, Supplier<RuntimeException> ex) {
        if (!Objects.equals(obj1, obj2)) {
            throw ex.get();
        }
    }

    /**
     * <p>Asserts that the obj1 is equals obj2, otherwise throw an exception.
     *
     * @param obj1 obj1
     * @param obj2 obj2
     * @param message exception message
     * @param args exception message arguments
     */
    public static void isEquals(Object obj1, Object obj2, String message, Object... args) {
        isEquals(obj1, obj2, () -> new Crane4jException(message, args));
    }

    /**
     * <p>Asserts that the obj1 is not equals obj2, otherwise throw an exception.
     *
     * @param obj1 obj1
     * @param obj2 obj2
     * @param ex exception supplier
     */
    public static void isNotEquals(Object obj1, Object obj2, Supplier<RuntimeException> ex) {
        if (Objects.equals(obj1, obj2)) {
            throw ex.get();
        }
    }

    /**
     * <p>Asserts that the obj1 is not equals obj2, otherwise throw an exception.
     *
     * @param obj1 obj1
     * @param obj2 obj2
     * @param message exception message
     * @param args exception message arguments
     */
    public static void isNotEquals(Object obj1, Object obj2, String message, Object... args) {
        isNotEquals(obj1, obj2, () -> new Crane4jException(message, args));
    }

    /**
     * <p>Asserts that the expression is true, otherwise throw an exception.
     *
     * @param expression expression
     * @param ex         exception supplier
     */
    public static void isTrue(boolean expression, Supplier<RuntimeException> ex) {
        if (!expression) {
            throw ex.get();
        }
    }

    /**
     * <p>Asserts that the expression is true, otherwise throw an exception.
     *
     * @param expression expression
     * @param message    exception message
     * @param args       exception message arguments
     */
    public static void isTrue(boolean expression, String message, Object... args) {
        isTrue(expression, () -> new Crane4jException(message, args));
    }


    /**
     * <p>Asserts that the expression is false, otherwise throw an exception.
     *
     * @param expression expression
     * @param ex         exception supplier
     */
    public static void isFalse(boolean expression, Supplier<RuntimeException> ex) {
        if (expression) {
            throw ex.get();
        }
    }

    /**
     * <p>Asserts that the expression is false, otherwise throw an exception.
     *
     * @param expression expression
     * @param message    exception message
     * @param args       exception message arguments
     */
    public static void isFalse(boolean expression, String message, Object... args) {
        isFalse(expression, () -> new Crane4jException(message, args));
    }

    /**
     * <p>Asserts that the object not empty, otherwise throw an exception.
     *
     * @param object object
     * @param ex     exception supplier
     */
    public static void isNotEmpty(Object object, Supplier<RuntimeException> ex) {
        if (object == null) {
            throw ex.get();
        }
        if ("".equals(object)) {
            throw ex.get();
        }
        if (object instanceof Collection && ((Collection<?>) object).isEmpty()) {
            throw ex.get();
        }
        if (object instanceof Map && ((Map<?, ?>) object).isEmpty()) {
            throw ex.get();
        }
        if (object instanceof Object[] && ((Object[]) object).length == 0) {
            throw ex.get();
        }
    }

    /**
     * <p>Asserts that the object not empty, otherwise throw an exception.
     *
     * @param object  object
     * @param message exception message
     * @param args    exception message arguments
     */
    public static void isNotEmpty(Object object, String message, Object... args) {
        isNotEmpty(object, () -> new Crane4jException(message, args));
    }

    /**
     * <p>Asserts that the object is empty, otherwise throw an exception.
     *
     * @param object object
     * @param ex     exception supplier
     */
    public static void isEmpty(Object object, Supplier<RuntimeException> ex) {
        if (object == null) {
            return;
        }
        if ("".equals(object)) {
            return;
        }
        if (object instanceof Collection && ((Collection<?>) object).isEmpty()) {
            return;
        }
        if (object instanceof Map && ((Map<?, ?>) object).isEmpty()) {
            return;
        }
        if (object instanceof Object[] && ((Object[]) object).length == 0) {
            return;
        }
        throw ex.get();
    }

    /**
     * <p>Asserts that the object is empty, otherwise throw an exception.
     *
     * @param object  object
     * @param message exception message
     * @param args    exception message arguments
     */
    public static void isEmpty(Object object, String message, Object... args) {
        isEmpty(object, () -> new Crane4jException(message, args));
    }

    /**
     * <p>Asserts that the object is null, otherwise throw an exception.
     *
     * @param object object
     * @param ex     exception supplier
     */
    public static void isNotNull(Object object, Supplier<RuntimeException> ex) {
        if (object == null) {
            throw ex.get();
        }
    }

    /**
     * <p>Asserts that the object is null, otherwise throw an exception.
     *
     * @param object  object
     * @param message exception message
     * @param args    exception message arguments
     */
    public static void isNotNull(Object object, String message, Object... args) {
        isNotNull(object, () -> new Crane4jException(message, args));
    }

    /**
     * <p>Asserts that the object is null, otherwise throw an exception.
     *
     * @param object object
     * @param ex     exception supplier
     */
    public static void isNull(Object object, Supplier<RuntimeException> ex) {
        if (object != null) {
            throw ex.get();
        }
    }

    /**
     * <p>Asserts that the object is null, otherwise throw an exception.
     *
     * @param object  object
     * @param message exception message
     * @param args    exception message arguments
     */
    public static void isNull(Object object, String message, Object... args) {
        isNull(object, () -> new Crane4jException(message, args));
    }
}
