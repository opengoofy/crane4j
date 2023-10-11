package cn.crane4j.benchmark;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 基准测试
 *
 * @author huangchengxing
 */
@SuppressWarnings("unused")
@Slf4j
@State(Scope.Benchmark)
@Measurement(iterations = 1, time = 5)
@Warmup(iterations = 1, time = 5)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkExample {

    @Param({"100", "1000", "10000", "100000", "1000000"})
    private int dataSize;
    private List<Foo> datas;
    private Map<Integer, Foo> datasource;
    private OperateTemplate operateTemplate;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BenchmarkExample.class.getSimpleName())
            .result("result.json")
            .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        this.operateTemplate = new OperateTemplate(
            configuration.getBeanOperationsParser(BeanOperationParser.class),
            configuration.getBeanOperationExecutor(BeanOperationExecutor.class),
            configuration.getTypeResolver()
        );

        // 初始化待填充数据
        this.datas = IntStream.rangeClosed(1, dataSize)
            .mapToObj(Foo::new)
            .collect(Collectors.toList());

        // 初始化数据源
        this.datasource = IntStream.rangeClosed(1, dataSize)
            .mapToObj(id -> new Foo(id, "name" + id, id % 100))
            .collect(Collectors.toMap(Foo::getId, Function.identity()));
        Container<Integer> container = Containers.forMap("test", this.datasource);
        configuration.registerContainer(container);
    }

    @Benchmark
    public void fillByCrane4j(Blackhole blackhole) {
        operateTemplate.execute(datas);
        blackhole.consume(datas);
    }

    @Benchmark
    public void fillBySpring(Blackhole blackhole) {
        datas.forEach(data -> {
            Foo foo = datasource.get(data.getId());
            BeanUtils.copyProperties(foo, data);
            blackhole.consume(data);
        });
    }

    @Benchmark
    public void fillByDefault(Blackhole blackhole) {
        datas.forEach(data -> {
            Foo foo = datasource.get(data.getId());
            data.setName(foo.getName());
            data.setAge(foo.getAge());
            blackhole.consume(data);
        });
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Foo {
        @Assemble(container = "test", props = {
            @Mapping("name"), @Mapping("age")
        })
        private final Integer id;
        private String name;
        private Integer age;
    }
}
