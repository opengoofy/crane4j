package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Declare a disassembly operation.<br />
 * Specify the specific properties of the current object as the nested object to be disassembled.
 * Before the assembly operation starts, the disassembly operation declared in the class
 * will be performed first, and the nested objects will be extracted and flattened at this time.
 *
 * <p>For example, we declare an assembly operation and a disassembly operation in the class of the following example：
 * <pre class="code">{@code
 * public class Foo {
 *     // operation of assemble
 *     @Assemble(namespace = "test", props = @Mapping(src = "name"))
 *     private Integer id;
 *
 *     // operation of disassemble
 *     @Dissassemble(type = Foo.class)
 *     private List<Foo> fooList;
 * }
 * }</pre>
 * When processing an object, the "foo List" in the object will be expanded and tiled first,
 * and then the nested objects in the <i>fooList</i> collection
 * will be assembled according to the configuration of Foo.class.
 *
 * <p>This annotation can support the processing of collections, arrays, or individual objects.<br />
 * When the type of the field to be disassembled cannot be determined,
 * the type no need to specify, for example, the type is generic or {@link Object}：
 * <pre class="code">{@code
 * public class Foo<T> {
 *     @Dissassemble
 *     private List<T> fooList;
 * }
 * }</pre>
 * During the subsequent processing, the actual type of the object will be dynamically inferred,
 * but there will be additional performance consumption for the fixed type.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.executor.handler.DisassembleOperationHandler;
 * @see cn.crane4j.core.parser.handler.DisassembleAnnotationHandler
 */
@Repeatable(value = Disassemble.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Disassemble {

    /**
     * Sort values.
     * The lower the value, the higher the priority.
     *
     * @return sort values
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * <p>Field name with nested objects to be disassembled.<br />
     * When the annotation is on an attribute, it is forced to specify the name of the annotated attribute.
     *
     * @return field name
     */
    String key() default "";

    /**
     * <p>The type of nested object.<br />
     * If the object is generic or cannot be confirmed, the default value is {@link Object},
     * and the type will be automatically inferred.
     *
     * @return type
     */
    Class<?> type() default Object.class;

    /**
     * The name of the handler to be used.
     *
     * @return name
     */
    String handler() default "ReflectiveDisassembleOperationHandler";

    /**
     * The group to which the current operation belongs.
     *
     * @return groups
     */
    String[] groups() default {};

    /**
     * Batch operation.
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        Disassemble[] value();
    }
}
