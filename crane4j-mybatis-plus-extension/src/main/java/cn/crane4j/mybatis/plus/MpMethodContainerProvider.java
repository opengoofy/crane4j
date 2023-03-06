package cn.crane4j.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.springboot.support.expression.SpelExpressionContext;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;

/**
 * Provider of {@link MpBaseMapperContainerRegister}.
 *
 * @author huangchengxing
 * @see MpBaseMapperContainerRegister
 */
@RequiredArgsConstructor
public class MpMethodContainerProvider implements ContainerProvider {

    private final ApplicationContext applicationContext;
    private final MpBaseMapperContainerRegister containerProcessor;
    private final ExpressionEvaluator evaluator;

    /**
     * <p>Get data source container, support obtain and call the specified method
     * in {@link BaseMapper}through incoming expression.
     *
     * <p>The input expression supports the following notation:
     * <ul>
     *     <li>
     *         {@code container('fooMapper', 'id', ['name', 'sex', 'age'])}, <br />
     *         equivalent to {@code select id, name, sex, age from foo where id in ?};
     *     </li>
     *     <li>
     *         {@code container('fooMapper', ['name', 'sex', 'age'])}, <br />
     *         equivalent to {@code select [pk], name, sex, age from foo where [pk] in ?};
     *     </li>
     *     <li>
     *         {@code container('fooMapper', 'id')}, <br />
     *         equivalent to {@code select * from foo where id in ?};
     *     </li>
     *     <li>
     *         {@code container('fooMapper')}, <br />
     *         equivalent to {@code select * from foo where [pk] in ?};
     *     </li>
     * </ul>
     * <b>NOTE:</b>When the query field is limited,
     * the query columns must contain the column of entered key.
     *
     * @param namespace namespace
     * @return container
     * @see MpBaseMapperContainerRegister#container
     */
    @Override
    public Container<?> getContainer(String namespace) {
        SpelExpressionContext context = new SpelExpressionContext(containerProcessor);
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        return evaluator.execute(namespace, Container.class, context);
    }
}
