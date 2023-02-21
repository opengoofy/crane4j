package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

/**
 * <p>Indicates that the annotation or the method pointed to by the annotation
 * can be converted to a method container of the specified type.
 * The annotated method needs to meet the following requirements：
 * <ul>
 *     <li>method must have a return value;</li>
 *     <li>If {@link #type()} is {@link MappingType#MAPPED}, the return value type must be {@link Map};</li>
 *     <li>
 *         If {@link #type()} is not {@link MappingType#MAPPED},
 *         the return value can be a single object, array or {@link Collection};
 *     </li>
 * </ul>
 *
 * <p>For example, the following example describes how to adapt the <i>requestFoo()</i> method to a data source container：<br />
 * The first way is to add annotations directly on the method：
 * <pre type="code">{@code
 * @ContainerMethod(
 *     namespace = "foo",
 *     resultType = Foo.class, resultKey = "id"
 * )
 * public List<Foo> requestFoo(Set<Integer> ids) { // do something }
 * }</pre>
 * The second way is to annotate the annotation on the class, and then bind the method through {@link Bind}：
 * <pre type="code">{@code
 * @ContainerMethod(
 *     namespace = "foo", resultType = Foo.class,
 *     bind = @Bind(value = "requestFoo", paramTypes = Set.class)
 * )
 * public class Foo {
 *     public List<Foo> requestFoo(Set<Integer> ids) { // do something }
 * }
 * }</pre>
 * The generated container namespace is <i>"foo"</i>.
 * When the id set is entered into the container, the Foo set grouped by <i>"id"</i> will be returned.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.container.MethodInvokerContainer
 * @see cn.crane4j.core.container.MethodContainerFactory
 */
@Repeatable(ContainerMethod.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerMethod {

    /**
     * Namespace of the data source container, use method name when empty.
     *
     * @return namespace
     */
    String namespace() default "";

    /**
     * The mapping relationship between the object returned by the method and the target object.
     *
     * @return mapping relationship
     */
    MappingType type() default MappingType.ONE_TO_ONE;

    /**
     * The key field of the data source object returned by the method.
     *
     * @return key field name
     */
    String resultKey() default "id";

    /**
     * Data source object type returned by method.
     *
     * @return type
     */
    Class<?> resultType();

    /**
     * When annotations are used on a class,
     * they are used to bind the corresponding methods in the class.
     *
     * @return 要查找的方法
     */
    Bind bind() default @Bind("");

    /**
     * Batch operation.
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ContainerMethod[] value() default {};
    }
}
