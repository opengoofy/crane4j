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
 * @see cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
 * @see cn.crane4j.core.parser.DisassembleOperation;
 */
@Repeatable(value = Disassemble.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Disassemble {

    /**
     * 排序值，在同一个类中，值越小的装配操作会越优先执行
     *
     * @return 排序值
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * <p>带有需要拆卸的嵌套对象的字段。<br />
     * 当注解注解在属性上时，会强制指定为被注解属性的名称。
     *
     * @return key字段名
     */
    String key() default "";

    /**
     * <p>嵌套对象的类型，该值可填其父类型。<br />
     * 若对象为泛型，或无法确认，则默认为{@link Object}，将会自动推断类型。
     *
     * @return 嵌套对象的类型
     */
    Class<?> type() default Object.class;

    /**
     * 用于完成当前操作的拆卸操作处理器
     *
     * @return 拆卸操作处理器类型
     */
    Class<?> handler() default Object.class;

    /**
     * 用于完成当前操作的拆卸操作处理器名称
     *
     * @return 用于完成当前操作的拆卸操作处理器名称
     */
    String handlerName() default "";

    /**
     * 用于解析嵌套对象操作配置的解析器类型
     *
     * @return 类型
     */
    Class<?> parser() default Object.class;

    /**
     * 用于解析嵌套对象操作配置的解析器类型
     *
     * @return 类型
     */
    String parserName() default "";

    /**
     * 当前操作所属的组别
     *
     * @return 组别
     */
    String[] groups() default {};

    /**
     * 批量操作
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        Disassemble[] value();
    }
}
