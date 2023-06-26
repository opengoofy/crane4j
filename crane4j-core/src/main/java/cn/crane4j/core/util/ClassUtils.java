package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * <p>{@link Class} utils.
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ClassUtils {

    /**
     * <p>Whether the given class is from packages
     * which package name is started with "java." or "javax.".
     *
     * @param clazz class
     * @return is jdk class
     */
    public static boolean isJdkClass(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        final Package objectPackage = clazz.getPackage();
        final String objectPackageName = objectPackage.getName();
        return objectPackageName.startsWith("java.")
            || objectPackageName.startsWith("javax.")
            || clazz.getClassLoader() == null;
    }

    /**
     * <p>Get class by class name.
     *
     * @param className class name
     * @return class comparator
     * @throws Crane4jException if class not found
     */
    public static Class<?> forName(String className) throws Crane4jException {
        Objects.requireNonNull(className);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new Crane4jException(e);
        }
    }

    /**
     * <p>Convert package path to resource path.<br />
     * eg: {@code cn.crane4j.core.util.ClassUtils -> cn/crane4j/core/util/ClassUtils}
     *
     * @param packagePath class path
     * @return resource path
     */
    public static String packageToPath(String packagePath) {
        Objects.requireNonNull(packagePath);
        return packagePath.replace(".", "/");
    }
}
