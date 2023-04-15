package cn.crane4j.annotation;

import java.lang.annotation.*;

/**
 * <p>Used to centrally configure a group of operations of assemble and disassemble on a class.<br />
 * It is generally used when it is inconvenient to
 * add {@link Assemble} and {@link Disassemble} directly on the attribute,
 * such as declaring operations based on the parent attribute in the subclass.
 *
 * @see Assemble
 * @see Disassemble
 * @see cn.crane4j.core.parser.AssembleAnnotationResolver;
 * @see cn.crane4j.core.parser.DisassembleAnnotationResolver;
 * @author huangchengxing
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Operations {

    /**
     * Operations of assemble.
     *
     * @return operations
     */
    Assemble[] assembles() default {};

    /**
     * Operations of disassemble.
     *
     * @return operations
     */
    Disassemble[] disassembles() default {};
}
