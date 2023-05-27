package cn.crane4j.core.executor.handler;

import cn.crane4j.core.support.reflect.PropertyOperator;

/**
 * An implementation of {@link AssembleOperationHandler}
 * for the one-to-one mapping between the target object and the data source object.
 *
 * @author huangchengxing
 */
public class OneToOneAssembleOperationHandler extends GenericAssembleOperationHandler {

    /**
     * Create an {@link ManyToManyAssembleOperationHandler} instance.
     *
     * @param propertyOperator property operator
     */
    public OneToOneAssembleOperationHandler(PropertyOperator propertyOperator) {
        super(propertyOperator);
    }
}
