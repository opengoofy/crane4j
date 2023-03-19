package cn.crane4j.core.executor.handler;

import cn.crane4j.core.support.reflect.PropertyOperator;

/**
 * An implementation of {@link AssembleOperationHandler}
 * for the one-to-one mapping between the target object and the data source object.
 *
 * @author huangchengxing
 */
public class OneToOneReflexAssembleOperationHandler extends AbstractReflexAssembleOperationHandler {

    /**
     * Create an {@link ManyToManyReflexAssembleOperationHandler} instance.
     *
     * @param propertyOperator property operator
     */
    public OneToOneReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        super(propertyOperator);
    }
}
