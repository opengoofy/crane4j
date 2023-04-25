package cn.crane4j.core.support.container;

import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * test for {@link AbstractQueryContainerCreator}
 *
 * @author huangchengxing
 */
public class AbstractQueryContainerCreatorTest {

    private static final Map<String, String> COLUMNS = new HashMap<String, String>(){{
        put("id", "user_id");
        put("name", "user_name");
        put("age", "user_age");
    }};
    private static final Map<String, String> QUERY_COLUMNS = new HashMap<String, String>(){{
        put("id", "user_id as id");
        put("name", "user_name as name");
        put("age", "user_age as age");
    }};

    private TestQueryContainerCreator containerCreator;

    @Before
    public void init() {
        this.containerCreator = new TestQueryContainerCreator(
            new MethodInvokerContainerCreator(new ReflectPropertyOperator())
        );
    }

    @Test
    public void test() {
        // check repository
        Object object = new Entity();
        containerCreator.registerRepository("test", object);
        AbstractQueryContainerCreator.Repository<Object> repository = containerCreator.registerRepositories.get("test");
        Assert.assertNotNull(repository);
        Assert.assertEquals("test", repository.getTableName());
        Assert.assertSame(object, repository.getTarget());
        Assert.assertEquals(object.getClass(), repository.getEntityType());
        Assert.assertEquals(MappingType.ONE_TO_ONE, repository.getMappingType(null));
        COLUMNS.forEach((p, c) -> Assert.assertEquals(c, repository.propertyToColumn(c, c)));
        QUERY_COLUMNS.forEach((p, c) -> Assert.assertEquals(c, repository.propertyToQueryColumn(c, c)));

        // check recorder
        containerCreator.getContainer("test", null, Collections.emptyList());
        Recorder recorder = containerCreator.recorders.get(0);
        checkRecorder(recorder, repository, null, Collections.emptyList());

        containerCreator.getContainer("test", "id", Arrays.asList("name", "age"));
        recorder = containerCreator.recorders.get(1);
        checkRecorder(recorder, repository, "id", Arrays.asList("name", "age"));

        containerCreator.getContainer("test", "id", Arrays.asList("id", "name", "age"));
        recorder = containerCreator.recorders.get(2);
        checkRecorder(recorder, repository, "id", Arrays.asList("id", "name", "age"));
    }

    private void checkRecorder(
        Recorder recorder, AbstractQueryContainerCreator.Repository<Object> repository,
        String keyProperty, List<String> properties) {

        keyProperty = CharSequenceUtil.emptyToDefault(keyProperty, repository.getKeyProperty());
        Assert.assertEquals(repository, recorder.getRepository());
        Assert.assertEquals(keyProperty, recorder.getKeyProperty());
        Assert.assertEquals(COLUMNS.get(keyProperty), recorder.getKeyColumn());

        Set<String> queryColumns = properties.stream()
            .map(QUERY_COLUMNS::get)
            .collect(Collectors.toSet());
        if (!properties.isEmpty()) {
            queryColumns.add(QUERY_COLUMNS.get(keyProperty));
            Assert.assertEquals(queryColumns, recorder.getQueryColumns());
        }
    }

    private static class TestQueryContainerCreator extends AbstractQueryContainerCreator<Object> {
        private final List<Recorder> recorders = new ArrayList<>();
        public TestQueryContainerCreator(MethodInvokerContainerCreator methodInvokerContainerCreator) {
            super(methodInvokerContainerCreator);
        }
        @Override
        protected Repository<Object> createRepository(String name, Object target) {
            return new TestRepository(target, name, "id");
        }
        @Nonnull
        @Override
        protected MethodInvoker createMethodInvoker(
            String namespace, Repository<Object> repository, Set<String> queryColumns, String keyColumn, String keyProperty) {
            Recorder recorder = new Recorder(namespace, repository, queryColumns, keyColumn, keyProperty);
            recorders.add(recorder);
            return recorder;
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class TestRepository implements AbstractQueryContainerCreator.Repository<Object> {
        private final Object target;
        private final String tableName;
        private final String keyProperty;

        @Override
        public Class<?> getEntityType() {
            return target.getClass();
        }

        @Override
        public String propertyToColumn(String property, String defaultValue) {
            return COLUMNS.getOrDefault(property, defaultValue);
        }
        @Override
        public String propertyToQueryColumn(String property, String defaultValue) {
            return QUERY_COLUMNS.getOrDefault(property, defaultValue);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class Recorder implements MethodInvoker {
        private final String namespace;
        private final AbstractQueryContainerCreator.Repository<Object> repository;
        private final Set<String> queryColumns;
        private final String keyColumn, keyProperty;
        @Override
        public Object invoke(Object target, Object... args) {
            return null;
        }
    }

    @Data
    private static class Entity {
        private Integer id;
        private String name;
        private Integer age;
    }
}
