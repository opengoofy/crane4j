package cn.createsequence.crane4j.core.executor;

import cn.createsequence.crane4j.core.exception.CraneException;
import cn.createsequence.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.createsequence.crane4j.core.parser.AssembleOperation;
import cn.createsequence.crane4j.core.parser.BeanOperations;
import cn.createsequence.crane4j.core.parser.DisassembleOperation;
import cn.createsequence.crane4j.core.parser.KeyTriggerOperation;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@link BeanOperationExecutor}的基本实现
 *
 * @author huangchengxing
 * @see AsyncBeanOperationExecutor
 * @see DisorderedBeanOperationExecutor
 * @see OrderedBeanOperationExecutor
 */
@Slf4j
public abstract class AbstractBeanOperationExecutor implements BeanOperationExecutor {

    /**
     * 根据指定的{@link BeanOperations}完成对{@code targets}中所有对象的操作
     *
     * @param targets 目标对象
     * @param operations 待执行的操作
     * @param filter 操作过滤器
     */
    @Override
    public void execute(Collection<?> targets, BeanOperations operations, Predicate<? super KeyTriggerOperation> filter) {
        if (CollUtil.isEmpty(targets) || Objects.isNull(operations)) {
            return;
        }
        Assert.isTrue(operations.isActive(), () -> new CraneException(
            "bean operation of [{}] is not activated", operations.getTargetType()
        ));
        // 若有必要，则先完成拆卸操作
        Multimap<BeanOperations, Object> collector = LinkedListMultimap.create();
        collector.putAll(operations, targets);
        disassembleIfNecessary(targets, operations, filter, collector);

        // 平摊后的对象，按装配操作分组并封装为装配执行对象
        List<AssembleExecution> executions = new ArrayList<>();
        collector.asMap().forEach((op, ts) -> op.getAssembleOperations()
            .stream()
            .filter(filter)
            .map(p -> createAssembleExecution(op, p, ts))
            .forEach(executions::add)
        );

        // 完成装配操作
        executeOperations(executions);
    }

    /**
     * 创建一个{@link AssembleExecution}。
     *
     * @param beanOperations 操作配置
     * @param operation 装配操作
     * @param targets 待处理的对象
     * @return {@link AssembleExecution}实例
     */
    protected AssembleExecution createAssembleExecution(
        BeanOperations beanOperations, AssembleOperation operation, Collection<Object> targets) {
        return new SimpleAssembleExecution(beanOperations, operation, targets);
    }
    
    /**
     * <p>完成装配操作。<br />
     * 入参的全部操作都保证他们在同一类中的有序性，比如：
     * 在{@code A.class}中存在有序操作<i>a</i>和<i>b</i>，
     * 则在获得对应的{@link AssembleExecution}时，依然保证<i>a</i>和<i>b</i>的顺序。
     *
     * @param executions 待完成的装配操作
     * @implNote
     * <ul>
     *     <li>若有必要，需要在此处保证{@link AssembleExecution}的执行顺序；</li>
     *     <li>若获取数据源时需要进行网络请求等长耗时操作，则需要尽可能的减少对数据源的请求次数；</li>
     * </ul>
     */
    protected abstract void executeOperations(List<AssembleExecution> executions);

    private static <T> void disassembleIfNecessary(
        Collection<T> targets, BeanOperations operations,
        Predicate<? super KeyTriggerOperation> filter, Multimap<BeanOperations, Object> collector) {
        Collection<DisassembleOperation> internalOperations = operations.getDisassembleOperations();
        if (CollUtil.isEmpty(internalOperations)) {
            return;
        }
        internalOperations.stream()
            .filter(filter)
            .forEach(internal -> doDisassembleAndCollect(targets, internal, filter, collector));
    }

    private static <T> void doDisassembleAndCollect(
        Collection<T> targets, DisassembleOperation disassembleOperation, Predicate<? super KeyTriggerOperation> filter, Multimap<BeanOperations, Object> collector) {
        DisassembleOperationHandler handler = disassembleOperation.getDisassembleOperationHandler();
        Collection<?> internalTargets = handler.process(disassembleOperation, targets);
        if (CollUtil.isEmpty(internalTargets)) {
            return;
        }
        BeanOperations internalOperations = disassembleOperation.getInternalBeanOperations(internalTargets);
        collector.putAll(internalOperations, internalTargets);
        // 如果嵌套对象内部仍然还有嵌套对象，则递归该拆卸过程
        disassembleIfNecessary(internalTargets, internalOperations, filter, collector);
    }

    /**
     * 尝试执行操作，若有必要，抛出异常时输出日志
     *
     * @param execute 待执行的操作
     */
    protected static void tryExecute(Runnable execute) {
        try {
            execute.run();
        } catch(Exception e) {
            log.warn("execute operation fail: {}", e.getMessage());
        }
    }
}
